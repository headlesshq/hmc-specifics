package me.earth.headlessmc.mc.player;

public class MockPlayer implements Player {
    public boolean dead;
    public int respawnRequests;

    @Override
    public void sendMessage(String message) {

    }

    @Override
    public void openMenu() {

    }

    @Override
    public void openInventory() {

    }

    @Override
    public void closeScreen() {

    }

    @Override
    public boolean isDead() {
        return dead;
    }

    @Override
    public void requestRespawn() {
        respawnRequests++;
    }

}
