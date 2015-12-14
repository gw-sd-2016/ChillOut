package com.example.grayapps.contextaware;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.CursorAdapter;
import android.support.v4.widget.SimpleCursorAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.example.grayapps.contextaware.dummy.DummyContent;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * A fragment representing a list of Items.
 * <p/>
 * Large screen devices (such as tablets) are supported by replacing the ListView
 * with a GridView.
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnFragmentInteractionListener}
 * interface.
 */
public class CalendarEventsListFragment extends ListFragment implements AbsListView.OnItemClickListener, LoaderManager.LoaderCallbacks<Cursor> {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private String mCurFilter;
    private static final int LOADER_EVENTS = 1;
    private static SimpleDateFormat msimpleDateFormat;

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    /**
     * The fragment's ListView/GridView.
     */
    private AbsListView mListView;

    /**
     * The Adapter which will be used to populate the ListView/GridView with
     * Views.
     */
    private SimpleCursorAdapter mAdapter;

    // TODO: Rename and change types of parameters
    public static CalendarEventsListFragment newInstance(String param1, String param2) {
        CalendarEventsListFragment fragment = new CalendarEventsListFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public CalendarEventsListFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            mListener = (OnFragmentInteractionListener) getActivity();

        } catch (ClassCastException e) {
            throw new ClassCastException(getActivity().toString()
                    + " must implement OnFragmentInteractionListener");
        }
        if(null != mListener)
            Log.d("Listener", "Is not null");
        else
        Log.d("Listener", "Is null");
        msimpleDateFormat = new SimpleDateFormat("h:mma");
        String from[] = new String[]{CalendarContract.Events.TITLE, CalendarContract.Events.EVENT_LOCATION, CalendarContract.Events.DTSTART, CalendarContract.Events.DTEND};
        int to[] = {R.id.eventTitle, R.id.eventLocation, R.id.eventStartTime, R.id.eventEndTime};

        getLoaderManager().initLoader(LOADER_EVENTS, getArguments(), this);

        mAdapter = new SimpleCursorAdapter(getActivity(), R.layout.calendarevent_list_item, null, from, to, CursorAdapter.NO_SELECTION);
        mAdapter.setViewBinder(new SimpleCursorAdapter.ViewBinder() {
            @Override
            public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
                if(columnIndex == 3)
                {
                    long startTime =  cursor.getLong(columnIndex);
                    String timeAsString = msimpleDateFormat.format(new Date(startTime));
                    TextView tView = (TextView) view;
                    tView.setText(timeAsString + " - ");
                    return true;
                }
                if(columnIndex == 4)
                {
                    long endTime =  cursor.getLong(columnIndex);
                    String timeAsString = msimpleDateFormat.format(new Date(endTime));
                    TextView tView = (TextView) view;
                    tView.setText(timeAsString);
                    return true;
                }
                return false;
            }
        });
        setListAdapter(mAdapter);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.calendar_event_list, container, false);

        // Set the adapter
        mListView = (AbsListView) view.findViewById(android.R.id.list);

        mListView.setAdapter(mAdapter);

        // Set OnItemClickListener so we can be notified on item clicks
        mListView.setOnItemClickListener(this);
        Log.d("IsClickable", "" + mListView.isClickable());
        Log.d("CreatedView", "MADE IT");
        return view;
    }

    @Override
    public void onAttach(Context context) {
        Log.d("OnAttach", "Is being called.");
        super.onAttach(context);
        try {
            Activity activity = (Activity) context;
            mListener = (OnFragmentInteractionListener) activity;

        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**/@Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Log.d("ItemClickedID", "WTF" + id);

        if (null != mListener) {
            TextView duration = (TextView) view.findViewById(R.id.eventStartTime);
            Log.d("Duration", String.valueOf(duration.getText()));
            Intent intent = new Intent(getContext(), EventDetailsActivity.class);
            startActivity(intent);
            mListener.onFragmentInteraction(DummyContent.ITEMS.get(position).id);
        }
    }

    /**/@Override
    public void onListItemClick(ListView listView, View view, int position, long id) {
        Log.d("ItemClickedID", "FTW" + id);

        if (null != mListener) {
            // Notify the active callbacks interface (the activity, if the
            // fragment is attached to one) that an item has been selected.
            TextView duration = (TextView) view.findViewById(R.id.eventStartTime);
            String dur = duration.getText().toString();
            Intent intent = new Intent(getContext(), EventDetailsActivity.class);
            intent.putExtra("eventId", id);
            startActivity(intent);
            //Listener.onFragmentInteraction(DummyContent.ITEMS.get(position).id);
        }
    }

    /**
     * The default content for this Fragment has a TextView that is shown when
     * the list is empty. If you would like to change the text, call this method
     * to supply the text it should use.
     */
    public void setEmptyText(CharSequence emptyText) {
        View emptyView = mListView.getEmptyView();

        if (emptyView instanceof TextView) {
            ((TextView) emptyView).setText(emptyText);
        }
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        public void onFragmentInteraction(String id);
    }

    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        switch (id) {
            case (LOADER_EVENTS):
                Uri uri = CalendarContract.Events.CONTENT_URI;
                String[] projection = new String[] {
                        CalendarContract.Events._ID,
                        CalendarContract.Events.TITLE,
                        CalendarContract.Events.EVENT_LOCATION,
                        CalendarContract.Events.DTSTART,
                        CalendarContract.Events.DTEND
                };

                int calendarId = args == null ? -1 : args.getInt("calendarId");
                Log.d("CalendarId", "" + calendarId);
                String query = CalendarContract.Events.ACCOUNT_NAME + " = ? AND " + CalendarContract.Events.DTEND + " < " + System.currentTimeMillis();
                return new CursorLoader(getActivity(), uri, projection, query, new String[] {"ajgray123@gmail.com"}, CalendarContract.Events.DTEND + " DESC");

        }

        return null;
    }

    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        // Swap the new cursor in.  (The framework will take care of closing the
        // old cursor once we return.)

        mAdapter.swapCursor(data);
    }

    public void onLoaderReset(Loader<Cursor> loader) {
        // This is called when the last Cursor provided to onLoadFinished()
        // above is about to be closed.  We need to make sure we are no
        // longer using it.
        mAdapter.swapCursor(null);
    }

}
