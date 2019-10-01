package dev.xibas.biometrics.presenter.proto;

import java.lang.ref.WeakReference;

public abstract class AbstractPresenter<V> {

    private WeakReference<V> view;

    public void attachView(V view) {
        this.view = new WeakReference<>(view);
        onViewAttached(view);
    }

    public void detachView(V view) {
        this.view = null;
        onViewDetached(view);
    }

    protected abstract void onViewAttached(V view);

    protected abstract void onViewDetached(V view);

    protected final V getView() {
        return view == null ? null : view.get();
    }

}
