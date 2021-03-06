package co.com.parsoniisolutions.custombottomsheetbehavior.lib.pager;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v4.view.PagerAdapter;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.Collection;

import co.com.parsoniisolutions.custombottomsheetbehavior.R;



public abstract class BottomSheetPagerAdapter extends PagerAdapter {

    // Views are slow to inflate, so let's keep a pool if Views for reuse
    private SoftCache<BottomSheetPage> mBottomSheetPageCache = new BottomSheetPageSoftCache( BottomSheetPage.class );

    // Let's remember which adapter position maps to which view
    private SparseArray<View> positionToViewMap = new SparseArray<>();

    protected WeakReference<BottomSheetViewPager> mViewPagerRef;
    public BottomSheetPagerAdapter( BottomSheetViewPager bottomSheetViewPager ) {
        mViewPagerRef = new WeakReference<>( bottomSheetViewPager );
    }
    protected BottomSheetViewPager viewPager() { return mViewPagerRef.get(); }

    @Override
    public View instantiateItem( ViewGroup container, int position ) {
        LayoutInflater inflater = (LayoutInflater)container.getContext().getSystemService( Context.LAYOUT_INFLATER_SERVICE );
        BottomSheetPage bottomSheetPage = mBottomSheetPageCache.get( inflater, this );
        View inflatedView = bottomSheetPage.inflatedView();
        container.addView( inflatedView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT );

        bottomSheetPage.setNewAdapterPosition( position );
        bottomSheetPage.setBottomSheetState( mViewPagerRef.get().targetBottomSheetState(), true );

        positionToViewMap.put( position, inflatedView );
        return inflatedView;
    }

    @Override
    public void destroyItem( ViewGroup container, int position, Object object ) {
        View view = (View) object;

        BottomSheetPage page = (BottomSheetPage)view.getTag( R.id.BOTTOM_SHEET_PAGE );
        page.onDestroy(); // Take care of closing cursors and any other cleanup

        container.removeView( view );

        positionToViewMap.remove( position );
        mBottomSheetPageCache.put( page );  // Save the view for recycling
    }

    public @Nullable BottomSheetPage getBottomSheetAtPosition( int position ) {
        View view = positionToViewMap.get( position );
        if ( view == null )
            return null;

        return (BottomSheetPage) view.getTag( R.id.BOTTOM_SHEET_PAGE );
    }

    public abstract BottomSheetPage createNewPage( LayoutInflater inflater );

    public SparseArray<View> allActiveViews() {
        return positionToViewMap;
    }
    public Collection<Reference<BottomSheetPage>> allCachedViews() {
        return mBottomSheetPageCache.peekAtCache();
    }


/*
    private Vector<BottomSheetBehaviorGoogleMapsLike.BottomSheetCallback> mBottomSheetStateCallbacks;
    public void addBottomSheetCallback( BottomSheetBehaviorGoogleMapsLike.BottomSheetCallback bottomSheetCallback ) {
        if ( mBottomSheetStateCallbacks == null ) {
            mBottomSheetStateCallbacks = new Vector<>();
        }

        mBottomSheetStateCallbacks.add( bottomSheetCallback );
    }
*/
/*
    public void onBottomSheetStateChanged( int newState, BottomSheetPage bottomSheetPage ) {
        for ( BottomSheetBehaviorGoogleMapsLike.BottomSheetCallback cb : mBottomSheetStateCallbacks ) {
            cb.onStateChanged( bottomSheetPage.inflatedView(), newState );
        }

        mBottomSheetState = newState;

        if ( !(newState == BottomSheetBehaviorGoogleMapsLike.STATE_COLLAPSED    ||
               newState == BottomSheetBehaviorGoogleMapsLike.STATE_ANCHOR_POINT ||
               newState == BottomSheetBehaviorGoogleMapsLike.STATE_EXPANDED )
                )
            return;

        // Iterate over all instantiated views
        for ( View view : positionToViewMap.values() ) {
            BottomSheetPage bsp = (BottomSheetPage) view.getTag( R.id.BOTTOM_SHEET_PAGE );
            if ( bsp == null )
                continue;
            if ( bsp == bottomSheetPage )
                continue;

            bsp.setBottomSheetState( newState, true );
        }
    }
*/
/*
    private int mSelectedPosition = 0;
    public void onPageSelected( int position ) {
        mSelectedPosition = position;
    }
    public int selectedPosition() {
        return mSelectedPosition;
    }

    private int mBottomSheetState = BottomSheetBehaviorGoogleMapsLike.STATE_COLLAPSED;
    public int bottomSheetState() {
        return mBottomSheetState;
    }
*/
}