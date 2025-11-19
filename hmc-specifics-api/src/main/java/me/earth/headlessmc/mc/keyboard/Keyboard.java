package me.earth.headlessmc.mc.keyboard;

public interface Keyboard extends Iterable<Key> {
    boolean isKeyDown(Key key);

    void press(Key key);

    void release(Key key);

    default boolean isKeyDown(int keyCode) {
        Key key = getKey(keyCode);
        return key != null && isKeyDown(key);
    }

    default Key getKey(int keyId) {
        Key scanCodeKey = null;
        for (Key key : this) {
            if (key.getId() == keyId) {
                return key;
            }

            if (key.getScanCode() == keyId) {
                scanCodeKey = key;
            }
        }

        return scanCodeKey;
    }

}
