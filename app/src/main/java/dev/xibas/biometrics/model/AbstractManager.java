package dev.xibas.biometrics.model;

import java.util.Map;
import java.util.WeakHashMap;

import dev.xibas.biometrics.model.Manager;

public abstract class AbstractManager implements Manager {

    private final Map<ManagerListener, Boolean> listeners = new WeakHashMap<>();

    @Override
    public void registerListener(ManagerListener listener) {
        synchronized (listeners) {
            if (listeners.containsKey(listener)) {
                return;
            }

            listeners.put(listener, true);
            listener.onManagerUpdate(this);
        }
    }

    @Override
    public void unregisterListener(ManagerListener listener) {
        synchronized (listeners) {
            listeners.remove(listener);
        }
    }

    protected synchronized void notifyUpdate() {
        synchronized (listeners) {
            for (ManagerListener listener : listeners.keySet()) {
                listener.onManagerUpdate(this);
            }
        }
    }

}
