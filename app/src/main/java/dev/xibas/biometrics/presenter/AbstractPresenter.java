package dev.xibas.biometrics.presenter;

import java.lang.ref.WeakReference;

public abstract class AbstractPresenter<V> implements Presenter<V> {

    private WeakReference<V> view;

    @Override
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
