package io.payex.android.ui.sale.history;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.graphics.drawable.VectorDrawableCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Random;

import butterknife.BindView;
import butterknife.ButterKnife;
import eu.davidea.flexibleadapter.FlexibleAdapter;
import eu.davidea.flexibleadapter.items.IFlexible;
import io.payex.android.R;
import io.payex.android.ui.common.CalendarFragment;
import io.payex.android.ui.common.ProgressItem;

import static android.content.ContentValues.TAG;

public class SaleHistoryFragment extends Fragment
//        implements CalendarFragment.OnCalendarInteractionListener
    implements SearchView.OnQueryTextListener,
        FlexibleAdapter.OnUpdateListener,
        FlexibleAdapter.EndlessScrollListener
{

    @BindView(R.id.rv_sale_history)
    RecyclerView mRecyclerView;

    @BindView(R.id.swipe_refresh_layout)
    SwipeRefreshLayout mSwipeRefreshLayout;

    @BindView(R.id.empty_view)
    View mEmptyView;

    private OnListFragmentInteractionListener mListener;
    private FlexibleAdapter<IFlexible> mAdapter;

    // note: remember to use this for filter as per guideline
    private List<IFlexible> mSaleHistoryClone = new ArrayList<>();

    public static final int DIALOG_FRAGMENT = 1;

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch(requestCode) {
            case DIALOG_FRAGMENT:
                if (resultCode == Activity.RESULT_OK) {
                    System.out.println("> " + data.getSerializableExtra("INPUT"));
                    List<Date> dates = (List<Date>)data.getSerializableExtra("INPUT");
                    for (Date d : dates) {
                        System.out.println(d.getTime());
                    }

                    // fixme filter by date range

//                    mAdapter.setSearchText(newText);
//                    //Fill and Filter mItems with your custom list and automatically animate the changes
//                    //Watch out! The original list must be a copy
//                    mAdapter.filterItems(mSaleHistoryClone, 200L);
                }
                break;
        }
    }



    public static SaleHistoryFragment newInstance() {
        return new SaleHistoryFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_sale_history, container, false);
        ButterKnife.bind(this, view);
        setHasOptionsMenu(true);

        Context context = view.getContext();
        mRecyclerView.setLayoutManager(new LinearLayoutManager(context));
        mRecyclerView.setHasFixedSize(true);

        final List<IFlexible> saleHistory = getSaleHistory();
        mSaleHistoryClone = saleHistory;

        //Initialize the Adapter
        mAdapter = new FlexibleAdapter<>(saleHistory);
        mAdapter.initializeListeners(new FlexibleAdapter.OnItemClickListener() {
            @Override
            public boolean onItemClick(int position) {
                mListener.onListFragmentInteraction(saleHistory.get(position));
                return false;
            }
        });

        //Initialize the RecyclerView and attach the Adapter to it as usual
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());

        //EndlessScrollListener - OnLoadMore (v5.0.0)
        mAdapter.setEndlessScrollListener(this, new ProgressItem());
        mAdapter.setEndlessScrollThreshold(1);//Default=1


        // init swipe
        mSwipeRefreshLayout.setEnabled(true);
        initializeSwipeToRefresh();

        return view;
    }

    public List<IFlexible> getSaleHistory() {
        List<IFlexible> list = new ArrayList<>();

        Drawable d = VectorDrawableCompat.create(getResources(), R.drawable.ic_mastercard_40dp, null);
        d = DrawableCompat.wrap(d);

        Calendar c = Calendar.getInstance();

        for (int i = 0; i < 25; i++) {
            c.add(Calendar.DATE, -i);

            list.add(new SaleHistoryItem(
                    i + 1 + "",
                    d,
                    "03:12",
                    "RM 30.00",
                    c.getTimeInMillis(),
                    getString(R.string.sale_history_card_ending)
            ));
        }
        return list;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnListFragmentInteractionListener) {
            mListener = (OnListFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnListFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface OnListFragmentInteractionListener {
        void onListFragmentInteraction(IFlexible item);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.menu_calendar) {
//            CalendarFragment f = new CalendarFragment();
//            f.show(getFragmentManager(), f.getTag());


            CalendarFragment f = CalendarFragment.newInstance();
            FragmentManager fm = getFragmentManager();
            f.setTargetFragment(this, DIALOG_FRAGMENT);
            f.show(fm, f.getTag());
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_sale_history, menu);
        initSearchView(menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    private void initSearchView(final Menu menu) {
        //Associate searchable configuration with the SearchView
//        SearchManager searchManager = (SearchManager) getContext().getSystemService(Context.SEARCH_SERVICE);
        MenuItem searchItem = menu.findItem(R.id.menu_search);
        if (searchItem != null) {
            SearchView mSearchView = (SearchView) MenuItemCompat.getActionView(searchItem);
            mSearchView.setInputType(InputType.TYPE_TEXT_VARIATION_FILTER);
            mSearchView.setImeOptions(EditorInfo.IME_ACTION_DONE | EditorInfo.IME_FLAG_NO_FULLSCREEN);
            mSearchView.setQueryHint(getString(R.string.sale_history_menu_search));
//            mSearchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
            mSearchView.setOnQueryTextListener(this);
        }
    }


    @Override
    public boolean onQueryTextChange(String newText) {
//        Log.e(TAG, "onQueryTextChange newText: " + newText);
        if (mAdapter.hasNewSearchText(newText)) {
            Log.e("SaleHistory", "onQueryTextChange newText: " + newText);
            mAdapter.setSearchText(newText);
            //Fill and Filter mItems with your custom list and automatically animate the changes
            //Watch out! The original list must be a copy
            mAdapter.filterItems(mSaleHistoryClone, 200L);
        }
        //Disable SwipeRefresh if search is active!!
//        mSwipeRefreshLayout.setEnabled(!mAdapter.hasSearchText());
        return true;
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        Log.v("SaleHistory", "onQueryTextSubmit called!");
        return onQueryTextChange(query);
    }


    // ==================== swipe refresh

    private void initializeSwipeToRefresh() {
        //Swipe down to force synchronize
        //mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipeRefreshLayout);
        mSwipeRefreshLayout.setDistanceToTriggerSync(390);
        //mSwipeRefreshLayout.setEnabled(true); Controlled by fragments!
        mSwipeRefreshLayout.setColorSchemeResources(
                android.R.color.holo_purple, android.R.color.holo_blue_light,
                android.R.color.holo_green_light, android.R.color.holo_orange_light);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                //Passing true as parameter we always animate the changes between the old and the new data set
                mAdapter.updateDataSet(getSaleHistory(), true);
                mSwipeRefreshLayout.setEnabled(false);

                // todo refresh here
                mRefreshHandler.sendEmptyMessageDelayed(0, 3000L);//Simulate network time
            }
        });
    }


    private final Handler mRefreshHandler = new Handler(Looper.getMainLooper(), new Handler.Callback() {
        public boolean handleMessage(Message message) {
            switch (message.what) {
                case 0: //Stop
                    mSwipeRefreshLayout.setRefreshing(false);
                    mSwipeRefreshLayout.setEnabled(true);
                    return true;
                case 1: //Start
                    mSwipeRefreshLayout.setRefreshing(true);
                    mSwipeRefreshLayout.setEnabled(false);
                    return true;
                case 2: //Show empty view
                    ViewCompat.animate(mEmptyView).alpha(1);
                    return true;
                default:
                    return false;
            }
        }
    });

    @Override
    public void onUpdateEmptyView(int size) {
        Log.d("SaleHistoryFragment", "onUpdateEmptyView size=" + size);

//        TextView emptyText = (TextView) findViewById(R.id.empty_text);
//        emptyText.setText(getString(R.string.no_items));
        if (size > 0) {
            mRefreshHandler.removeMessages(2);
            mEmptyView.setAlpha(0);
        } else {
            mEmptyView.setAlpha(0);
            mRefreshHandler.sendEmptyMessage(2);
        }
    }


    // ===================== LOAD MORE

    @Override
    public void onLoadMore() {
        //We don't want load more items when searching into the current Collection!
        //Alternatively, for a special filter, if we want load more items when filter is active, the
        // new items that arrive from remote, should be already filtered, before adding them to the Adapter!
        if (mAdapter.hasSearchText()) {
            mAdapter.onLoadMoreComplete(null);
            return;
        }
        Log.i(TAG, "onLoadMore invoked!");
        //Simulating asynchronous call
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                final List<IFlexible> newItems = new ArrayList<>();

                //Simulating success/failure
                int count = new Random().nextInt(5);
                int totalItemsOfType = mAdapter.getItemCountOfTypes(R.layout.fragment_sale_history_item);
                List<IFlexible> fakeItems = getSaleHistory();
                for (int i = 1; i <= count; i++) {
                    newItems.add(fakeItems.get(i));
                }

                //Callback the Adapter to notify the change:
                //- New items will be added to the end of the list
                //- When list is null or empty, ProgressItem will be hidden
                mAdapter.onLoadMoreComplete(newItems);
                for (IFlexible item : newItems) {
                    mAdapter.addItem(mAdapter.getItemCount(), item);
                }

                //Notify user
                String message = (newItems.size() > 0 ?
                        "Simulated: " + newItems.size() + " new items arrived :-)" :
                        "Simulated: No more items to load :-(");
                Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
            }
        }, 2500);
    }


}
