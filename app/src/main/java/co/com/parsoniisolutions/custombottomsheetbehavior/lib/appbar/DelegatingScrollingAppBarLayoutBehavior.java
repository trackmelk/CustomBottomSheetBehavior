package co.com.parsoniisolutions.custombottomsheetbehavior.lib.appbar;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.view.NestedScrollingParent;
import android.util.AttributeSet;
import android.view.View;

import org.greenrobot.eventbus.EventBus;

import java.lang.ref.WeakReference;

import co.com.parsoniisolutions.custombottomsheetbehavior.lib.behaviors.BottomSheetBehaviorGoogleMapsLike;
import co.com.parsoniisolutions.custombottomsheetbehavior.lib.utils.DimensionUtils;


/**
 *  Behavior applied on an AppBarLayout within a ViewPager. It delegates visibility change actions to ScrollAppBarLayout.
 */
public class DelegatingScrollingAppBarLayoutBehavior extends DelegatingAppBarLayoutBehavior {

    private boolean mInit = false;
    private boolean mVisible = true;
    /**
     * To avoid using multiple "peekheight=" in XML and looking flexibility allowing {@link BottomSheetBehaviorGoogleMapsLike#mPeekHeight}
     * get changed dynamically we get the {@link android.support.v4.view.NestedScrollingParent} that has
     * "app:layout_behavior=" {@link BottomSheetBehaviorGoogleMapsLike} inside the {@link CoordinatorLayout}
     */
    private WeakReference<BottomSheetBehaviorGoogleMapsLike> mBottomSheetBehaviorRef;

    public DelegatingScrollingAppBarLayoutBehavior( Context context, AttributeSet attrs ) {
        super( context, attrs );
    }

    @Override
    public boolean layoutDependsOn( CoordinatorLayout parent, View child, View dependency ) {
        if ( dependency instanceof NestedScrollingParent ) {
            try {
                BottomSheetBehaviorGoogleMapsLike.from( dependency );
                return true;
            }
            catch ( IllegalArgumentException e ) { }
        }
        return false;
    }

    @Override
    public boolean onDependentViewChanged( CoordinatorLayout parent, View child, View dependency ) {
        if ( ! mInit ) {
            return init( parent, child, dependency );
        }
        if ( mBottomSheetBehaviorRef == null  ||  mBottomSheetBehaviorRef.get() == null )
            getBottomSheetBehavior( parent );

        boolean appBarVisible = dependency.getY() >= dependency.getHeight() - mBottomSheetBehaviorRef.get().getPeekHeight() - DimensionUtils.getStatusBarHeight( parent.getContext() );

        if ( publish() ) {
            EventBus.getDefault().post( new EventScrollAppBarVisibility( appBarVisible, parent ) );
        }

        return true;
    }

    @Override
    public Parcelable onSaveInstanceState( CoordinatorLayout parent, View child ) {
        return new SavedState(super.onSaveInstanceState(parent, child), mVisible);
    }

    @Override
    public void onRestoreInstanceState( CoordinatorLayout parent, View child, Parcelable state ) {
        SavedState ss = (SavedState) state;
        super.onRestoreInstanceState( parent, child, ss.getSuperState() );
        this.mVisible = ss.mVisible;
    }

    private boolean init( CoordinatorLayout parent, View child, View dependency ) {

        /**
         * First we need to know if dependency view is upper or lower compared with
         * {@link BottomSheetBehaviorGoogleMapsLike#getPeekHeight()} Y position to know if need to show the AppBar at beginning.
         */
        getBottomSheetBehavior(parent);
        if (mBottomSheetBehaviorRef == null || mBottomSheetBehaviorRef.get() == null)
            getBottomSheetBehavior(parent);
        int mCollapsedY = dependency.getHeight() - mBottomSheetBehaviorRef.get().getPeekHeight();
        mVisible = (dependency.getY() >= mCollapsedY);

        if ( publish() ) {
            EventBus.getDefault().post( new EventScrollAppBarVisibility( mVisible, parent ) );
        }

        mInit = true;
        /**
         * Following {@link #onDependentViewChanged} docs, we need to return true if the
         * Behavior changed the child view's size or position, false otherwise.
         * In our case we only move it if mVisible got false in this method.
         */
        return !mVisible;
    }

    /**
     * Look into the CoordinatorLayout for the {@link BottomSheetBehaviorGoogleMapsLike}
     * @param coordinatorLayout with app:layout_behavior= {@link BottomSheetBehaviorGoogleMapsLike}
     */
    private void getBottomSheetBehavior( @NonNull CoordinatorLayout coordinatorLayout ) {

        for (int i = 0; i < coordinatorLayout.getChildCount(); i++) {
            View child = coordinatorLayout.getChildAt(i);

            if (child instanceof NestedScrollingParent ) {

                try {
                    BottomSheetBehaviorGoogleMapsLike temp = BottomSheetBehaviorGoogleMapsLike.from(child);
                    mBottomSheetBehaviorRef = new WeakReference<>(temp);
                    break;
                }
                catch (IllegalArgumentException e){}
            }
        }
    }

    protected static class SavedState extends View.BaseSavedState {

        final boolean mVisible;

        public SavedState(Parcel source) {
            super(source);
            mVisible = source.readByte() != 0;
        }

        public SavedState(Parcelable superState, boolean visible) {
            super(superState);
            this.mVisible = visible;
        }

        @Override
        public void writeToParcel(Parcel out, int flags) {
            super.writeToParcel(out, flags);
            out.writeByte((byte) (mVisible ? 1 : 0));
        }

        public static final Creator<SavedState> CREATOR =
                new Creator<SavedState>() {
                    @Override
                    public SavedState createFromParcel(Parcel source) {
                        return new SavedState(source);
                    }

                    @Override
                    public SavedState[] newArray(int size) {
                        return new SavedState[size];
                    }
                };
    }
}