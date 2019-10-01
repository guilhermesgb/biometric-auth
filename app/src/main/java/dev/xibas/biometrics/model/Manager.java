package dev.xibas.biometrics.model;

public interface Manager {

    interface ManagerListener {
        void onManagerUpdate(Manager manager);
    }

    void registerListener(ManagerListener listener);

    void unregisterListener(ManagerListener listener);

}
