package com.vexsoftware.votifier.bungee;

import net.md_5.bungee.api.event.ProxyReloadEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

public class ReloadListener implements Listener {
    private final Oyvotifer oyvotifer;

    public ReloadListener(Oyvotifer oyvotifer) {
        this.oyvotifer = oyvotifer;
    }

    @EventHandler
    public void onProxyReload(ProxyReloadEvent event) {
        this.oyvotifer.reload();
    }
}
