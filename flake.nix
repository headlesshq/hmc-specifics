{
  description = "Development shells for hmc-specifics";

  inputs.nixpkgs.url = "github:NixOS/nixpkgs/nixos-26.05";

  outputs =
    { nixpkgs, ... }:
    let
      inherit (nixpkgs) lib;

      systems = [
        "x86_64-linux"
        "aarch64-linux"
        "aarch64-darwin"
      ];

      forAllSystems = f: lib.genAttrs systems (system: f nixpkgs.legacyPackages.${system});

      # nixpkgs has no JDK 16 any more. Like the devcontainer, use the final
      # Temurin 16 release from Adoptium, packaged with nixpkgs' own Temurin
      # builder. Temurin 16 has no aarch64-darwin build, so 17 stands in there.
      temurin-bin-16 =
        pkgs:
        let
          # jdk-linux-base.nix is internal to nixpkgs, not a public API. After
          # every flake.lock bump, re-check that this path and its arguments
          # still exist. If the file is gone, 17 stands in with a warning.
          base = "${nixpkgs}/pkgs/development/compilers/temurin-bin/jdk-linux-base.nix";
          jdk16 = pkgs.callPackage (import base {
            sourcePerArch = {
              packageType = "jdk";
              vmType = "hotspot";
              x86_64 = {
                version = "16.0.2";
                url = "https://github.com/adoptium/temurin16-binaries/releases/download/jdk-16.0.2%2B7/OpenJDK16U-jdk_x64_linux_hotspot_16.0.2_7.tar.gz";
                sha256 = "323d6d7474a359a28eff7ddd0df8e65bd61554a8ed12ef42fd9365349e573c2c";
              };
              aarch64 = {
                version = "16.0.2";
                url = "https://github.com/adoptium/temurin16-binaries/releases/download/jdk-16.0.2%2B7/OpenJDK16U-jdk_aarch64_linux_hotspot_16.0.2_7.tar.gz";
                sha256 = "cb77d9d126f97898dfdc8b5fb694d1e0e5d93d13a0a6cb2aeda76f8635384340";
              };
            };
          }) { };
        in
        if !pkgs.stdenv.hostPlatform.isLinux then
          pkgs.temurin-bin-17
        else if builtins.pathExists base then
          jdk16
        else
          lib.warn "hmc-specifics: ${base} is missing, using JDK 17 for Java 16" pkgs.temurin-bin-17;

      # The "java" values of ci-data.json. Temurin 8 has no aarch64-darwin
      # build, so Zulu 8 stands in there.
      jdks = pkgs: {
        "8" =
          if lib.meta.availableOn pkgs.stdenv.hostPlatform pkgs.temurin-bin-8 then
            pkgs.temurin-bin-8
          else
            pkgs.zulu8;
        "16" = temurin-bin-16 pkgs;
        "17" = pkgs.temurin-bin-17;
        "21" = pkgs.temurin-bin-21;
        "25" = pkgs.temurin-bin-25;
      };

      # Builds one directory of ci-data.json with the JDK that CI uses for it:
      #   hmc-build <dir|mc-version> [gradle args...]
      # A directory (hmc-specifics-api, versions/1_12 or 1_12) builds with the
      # defaults from its gradle.properties. A Minecraft version (1.21.4) picks
      # its ci-data.json entry and passes its versions as Gradle properties.
      mkBuildHelper =
        pkgs:
        pkgs.writeShellApplication {
          name = "hmc-build";
          runtimeInputs = [ pkgs.jq ];
          text = ''
            if [ $# -lt 1 ]; then
              echo "usage: hmc-build <dir|mc-version> [gradle args...]" >&2
              exit 2
            fi

            root=$PWD
            while [ ! -f "$root/ci-data.json" ]; do
              [ "$root" != / ] || { echo "ci-data.json not found" >&2; exit 1; }
              root=$(dirname "$root")
            done

            target=$1
            shift
            [ $# -gt 0 ] || set -- build

            entry=$(jq -c --arg t "$target" '
              [.build_data[] | select(.mc == $t)][0]
              // [.build_data[] | select(.dir == $t or .dir == "versions/" + $t)
                                | del(.mc, .lex, .neo)][0]
              // empty' "$root/ci-data.json")
            if [ -z "$entry" ]; then
              echo "No entry for '$target' in ci-data.json" >&2
              exit 1
            fi

            dir=$(jq -r .dir <<< "$entry")
            java=$(jq -r .java <<< "$entry")
            props=()
            for p in mc:minecraft_version lex:lexforge_version neo:neoforge_version; do
              value=$(jq -r --arg k "''${p%%:*}" '.[$k] // empty' <<< "$entry")
              [ -z "$value" ] || props+=("-P''${p#*:}=$value")
            done

            case "$java" in
            ${lib.concatStrings (
              lib.mapAttrsToList (version: jdk: ''
                ${version}) JAVA_HOME=${jdk.home} ;;
              '') (jdks pkgs)
            )}
              *)
                echo "No Java $java in this flake" >&2
                exit 1
                ;;
            esac

            export JAVA_HOME
            export PATH="$JAVA_HOME/bin:$PATH"
            echo "Building $dir with Java $java ($JAVA_HOME): ''${props[*]} $*" >&2
            cd "$root/$dir"
            exec sh ./gradlew "''${props[@]}" "$@"
          '';
        };

      mkJavaShell =
        pkgs: primary:
        let
          available = jdks pkgs;
          jdk = available.${primary};
        in
        pkgs.mkShell (
          {
            packages = [
              jdk
              pkgs.jq
              (mkBuildHelper pkgs)
            ];

            JAVA_HOME = jdk.home;
          }
          // lib.optionalAttrs (lib.length (lib.attrNames available) > 1) {
            # Gradle ignores org.gradle.java.installations.paths in GRADLE_OPTS,
            # but its toolchain detection scans $SDKMAN_CANDIDATES_DIR/java/*
            SDKMAN_CANDIDATES_DIR = pkgs.linkFarm "jdks" (
              lib.mapAttrs' (version: j: lib.nameValuePair "java/${version}" j.home) available
            );
          }
        );
    in
    {
      devShells = forAllSystems (
        pkgs:
        {
          default = mkJavaShell pkgs "25";
        }
        // lib.mapAttrs' (version: _: lib.nameValuePair "java${version}" (mkJavaShell pkgs version)) (
          jdks pkgs
        )
      );

      formatter = forAllSystems (pkgs: pkgs.nixfmt-tree);
    };
}
