package com.example.grayapps.contextaware;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.CursorAdapter;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;
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
public class CalendarEventsListFragment extends ListFragment implements AbsListView.OnItemClickListener, LoaderManager.LoaderCallbacks<Cursor>
{

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private String mCurFilter;
    private static final int LOADER_EVENTS = 1;
    private static SimpleDateFormat mSimpleDateFormat;
    private static SimpleDateFormat mDayOfWeekFormat;
    private static CalendarEventRecordingTrigger mCurrentEvent;
    private static double[] mEventData;
    private static JSONObject mEventMap;
    private static String mCurrentWeek;
    private static String mCurrentDay;
    private static Calendar mCalendar;
    private static long mEndTime;

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
    private EventsAdapter mAdapter;

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
        mEventData = new double[3];
        mCalendar = Calendar.getInstance();
        mCurrentWeek = "";
        mCurrentDay = "";
        mEndTime = Long.MAX_VALUE;

        getEventMap();
        try
        {
            mListener = (OnFragmentInteractionListener) getActivity();

        } catch (ClassCastException e)
        {
            throw new ClassCastException(getActivity().toString()
                    + " must implement OnFragmentInteractionListener");
        }
        if (null != mListener)
            Log.d("Listener", "Is not null");
        else
            Log.d("Listener", "Is null");

        mSimpleDateFormat = new SimpleDateFormat("h:mma");
        mDayOfWeekFormat = new SimpleDateFormat("cccc");

        String from[] = new String[]{CalendarContract.Events.TITLE, CalendarContract.Events.EVENT_LOCATION, CalendarContract.Events.DTSTART, CalendarContract.Events.DTEND};
        int to[] = {R.id.eventTitle, R.id.eventLocation, R.id.eventStartTime, R.id.eventEndTime};

        getLoaderManager().initLoader(LOADER_EVENTS, getArguments(), this);
        mAdapter = new EventsAdapter(getActivity(), R.layout.calendarevent_list_item, null, from, to, getLayoutInflater(savedInstanceState));
        setListAdapter(mAdapter);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.calendar_event_list, container, false);

        // Set the adapter
        mListView = (AbsListView) view.findViewById(android.R.id.list);
        final Bundle tempBundle = savedInstanceState;
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
        try
        {
            Activity activity = (Activity) context;
            mListener = (OnFragmentInteractionListener) activity;

        } catch (ClassCastException e)
        {
            throw new ClassCastException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**/
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Log.d("ItemClickedID", "WTF" + id);

        if (null != mListener)
        {
            TextView duration = (TextView) view.findViewById(R.id.eventStartTime);
            Log.d("Duration", String.valueOf(duration.getText()));
            Intent intent = new Intent(getContext(), EventDetailsActivity.class);
            startActivity(intent);
        }
    }

    /**/
    @Override
    public void onListItemClick(ListView listView, View view, int position, long id) {
        Log.d("ItemClickedID", "FTW" + id);

        if (null != mListener)
        {
            // Notify the active callbacks interface (the activity, if the
            // fragment is attached to one) that an item has been selected.
            TextView duration = (TextView) view.findViewById(R.id.eventStartTime);
//            String dur = duration.getText().toString();
            Log.d("ItemClicked", String.valueOf(id));
            Intent intent = new Intent(getContext(), EventDetailsActivity.class);
            mEventData[0] = -1;
            mEventData[1] = -1;
            mEventData[2] = -1;
            int c = mAdapter.getEventColor(String.valueOf(id));
            intent.putExtra("eventId", String.valueOf(id));
            intent.putExtra("position", position);
            if (mEventData[0] > 0)
            {
                intent.putExtra("stress", mEventData[0]);
                intent.putExtra("noise", mEventData[1]);
                intent.putExtra("movement", mEventData[2]);
            }

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

        if (emptyView instanceof TextView)
        {
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
    public interface OnFragmentInteractionListener
    {
        // TODO: Update argument type and name
        public void onFragmentInteraction(String id);
    }

    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        switch (id)
        {
            case (LOADER_EVENTS):
                Uri uri = CalendarContract.Events.CONTENT_URI;
                String[] projection = new String[]{
                        CalendarContract.Events._ID,
                        CalendarContract.Events.TITLE,
                        CalendarContract.Events.EVENT_LOCATION,
                        CalendarContract.Events.DTSTART,
                        CalendarContract.Events.DTEND
                };

                int calendarId = args == null ? -1 : args.getInt("calendarId");
                Log.d("CalendarId", "" + calendarId);
                String query = CalendarContract.Events.ACCOUNT_NAME + " = ? AND " + CalendarContract.Events.DTEND + " < " + System.currentTimeMillis();
                return new CursorLoader(getActivity(), uri, projection, query, new String[]{"ajgray123@gmail.com"}, CalendarContract.Events.DTEND + " DESC");

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

    public void getEventMap()
    {
        SharedPreferences savedData = getActivity().getSharedPreferences("userData", Context.MODE_PRIVATE);
        String objectID = savedData.getString("parseEventMapID", "ID Not Available");
        final Gson converter = new Gson();
        if (!objectID.equals("ID Not Available"))
        {
            ParseQuery<ParseObject> query = ParseQuery.getQuery("CalendarEvents");
            query.fromLocalDatastore();
            try
            {
                ParseObject object = query.get(objectID);
                mEventMap = object.getJSONObject("EventMapWrapper");
            } catch (ParseException e)
            {
                Log.d("EventMapWrapper", "Not Found");
            }
        }
    }

    private class EventsAdapter extends SimpleCursorAdapter
    {
        private int mLayoutId;
        private CardView mCurrentCard;
        private LinearLayout mInnerCardLayout;
        private String mRecentDate;
        private final SimpleDateFormat mDateComparer = new SimpleDateFormat("MMM dd, yyyy");
        private LayoutInflater mInflater;

        public EventsAdapter(Context context, int layout, Cursor cursor, String[] from,
                             int[] to, LayoutInflater inflater)
        {
            super(getActivity(), layout, cursor, from, to, CursorAdapter.NO_SELECTION);
            mInflater = inflater;
            mRecentDate = mDateComparer.format(new Date(System.currentTimeMillis()));
            mCurrentCard = new CardView(inflater.getContext(), null, 0);
            mInnerCardLayout = new LinearLayout(inflater.getContext());
            mLayoutId = layout;
            setDropDownViewResource(R.layout.calendarevent_card_item);
        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent)
        {
            return mInflater.inflate(R.layout.calendarevent_list_item, parent, false);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent)
        {
            Cursor c = getCursor();
            View rowView = super.getView(position, convertView, parent);
            CardView card = (CardView) rowView.findViewById(R.id.cardView);
            FrameLayout buffer = (FrameLayout) rowView.findViewById(R.id.buffer);
            int col = getEventColor(c.getString(0));
            if (col > 0)
            {
                if (col == 2)
                {
                    buffer.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.colorStress));
                    card.setCardBackgroundColor(ContextCompat.getColor(getContext(), R.color.colorStress));
                } else if (col == 1)
                {
                    buffer.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.colorNoise));
                    card.setCardBackgroundColor(ContextCompat.getColor(getContext(), R.color.colorNoise));
                }
            } else
            {
                if (position % 3 == 0)
                {
                    buffer.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.colorStress));
                    card.setCardBackgroundColor(ContextCompat.getColor(getContext(), R.color.colorStress));
                } else if (position % 5 == 0)
                {
                    buffer.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.colorNoise));
                    card.setCardBackgroundColor(ContextCompat.getColor(getContext(), R.color.colorNoise));
                } else if (position % 7 == 0 || position % 4 == 0)
                {
                    buffer.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.colorAnxious));
                    card.setCardBackgroundColor(ContextCompat.getColor(getContext(), R.color.colorAnxious));
                } else
                {
                    buffer.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.colorNeutral));
                    card.setCardBackgroundColor(ContextCompat.getColor(getContext(), R.color.colorNeutral));
                }
            }

            return rowView;
        }

        @Override
        public void bindView(View rowView, Context context, Cursor cursor) {

            int color = getEventColor(cursor.getString(0));
            CardView card = (CardView) rowView.findViewById(R.id.cardView);
            CardView cardWrapper = (CardView) rowView.findViewById(R.id.backgroundCard);
            String title = cursor.getString(1);
            String location = cursor.getString(2);
            long startTime = cursor.getLong(3);
            long endTime = cursor.getLong(4);

            mCalendar.setTimeInMillis(startTime);
            mCalendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
            String dateString = "Week of " + mDateComparer.format(mCalendar.getTime());
            TextView date = (TextView) rowView.findViewById(R.id.eventDate);
            FrameLayout header = (FrameLayout) rowView.findViewById(R.id.header);
            int margin = (int) mContext.getResources().getDimension(R.dimen.card_margin);
            FrameLayout divider = (FrameLayout) rowView.findViewById(R.id.divider);
            divider.setVisibility(View.VISIBLE);
            if (cursor.getPosition() > 0 && cursor.moveToPrevious())
            {
                mCalendar.setTimeInMillis(cursor.getLong(3));
                mCalendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
                String prevString = "Week of " + mDateComparer.format(mCalendar.getTime());
                if (prevString.equals(dateString))
                {

                    header.setVisibility(View.GONE);
                    LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) cardWrapper.getLayoutParams();
                    lp.setMargins(0, 0, 0, 0);
                    cursor.moveToNext();
                    if (cursor.getPosition() < cursor.getCount() - 1)
                        if (cursor.moveToNext())
                        {
                            mCalendar.setTimeInMillis(cursor.getLong(3));
                            mCalendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
                            String nextString = "Week of " + mDateComparer.format(mCalendar.getTime());
                            FrameLayout buffer = (FrameLayout) rowView.findViewById(R.id.buffer);
                            buffer.setVisibility(View.VISIBLE);
                            FrameLayout.LayoutParams fb = (FrameLayout.LayoutParams) buffer.getLayoutParams();
                            if (nextString.equals(dateString))
                            {
                                card.setRadius(0);
                                fb.gravity = Gravity.BOTTOM;
                            } else
                            {
                                card.setRadius(mContext.getResources().getDimension(R.dimen.card_radius));
                                lp.setMargins(0, 0, 0, margin);
                                fb.gravity = Gravity.TOP;
                                divider.setVisibility(View.GONE);
                            }
                            cursor.moveToPrevious();
                        }
                } else
                {
                    header.setVisibility(View.VISIBLE);
                    date.setText(dateString);
                    mCurrentWeek = dateString;
                    LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) cardWrapper.getLayoutParams();
                    card.setRadius(mContext.getResources().getDimension(R.dimen.card_radius));
                    cursor.moveToNext();
                    if (cursor.getPosition() < cursor.getCount() - 1)
                        if (cursor.moveToNext())
                        {
                            mCalendar.setTimeInMillis(cursor.getLong(3));
                            mCalendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
                            String nextString = "Week of " + mDateComparer.format(mCalendar.getTime());
                            FrameLayout buffer = (FrameLayout) rowView.findViewById(R.id.buffer);
                            if (!nextString.equals(dateString))
                            {
                                card.setRadius(mContext.getResources().getDimension(R.dimen.card_radius));
                                buffer.setVisibility(View.GONE);
                                lp.setMargins(0, margin, 0, margin);
                                divider.setVisibility(View.GONE);
                            } else
                            {
                                FrameLayout.LayoutParams fb = (FrameLayout.LayoutParams) buffer.getLayoutParams();
                                buffer.setVisibility(View.VISIBLE);
                                fb.gravity = Gravity.BOTTOM;
                                lp.setMargins(0, margin, 0, 0);
                            }
                            cursor.moveToPrevious();
                        }
                }

            } else
            {
                header.setVisibility(View.VISIBLE);
                date.setText(dateString);
                mCurrentWeek = dateString;
                LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) cardWrapper.getLayoutParams();
                card.setRadius(mContext.getResources().getDimension(R.dimen.card_radius));
                if (cursor.getPosition() < cursor.getCount() - 1)
                    if (cursor.moveToNext())
                    {
                        mCalendar.setTimeInMillis(cursor.getLong(3));
                        mCalendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
                        String nextString = "Week of " + mDateComparer.format(mCalendar.getTime());
                        FrameLayout buffer = (FrameLayout) rowView.findViewById(R.id.buffer);
                        if (!nextString.equals(dateString))
                        {
                            // card.setRadius(0);
                            lp.setMargins(0, margin, 0, margin);
                            buffer.setVisibility(View.GONE);
                            // divider.setVisibility(View.GONE);
                        } else
                        {
                            FrameLayout.LayoutParams fb = (FrameLayout.LayoutParams) buffer.getLayoutParams();
                            buffer.setVisibility(View.VISIBLE);
                            fb.gravity = Gravity.BOTTOM;
                            lp.setMargins(0, margin, 0, 0);
                        }
                        cursor.moveToPrevious();
                    }
            }

            TextView eventTitle = (TextView) rowView.findViewById(R.id.eventTitle);
            TextView eventLocation = (TextView) rowView.findViewById(R.id.eventLocation);
            TextView eventDayOfWeek = (TextView) rowView.findViewById(R.id.eventDayOfWeek);
            TextView eventStartTime = (TextView) rowView.findViewById(R.id.eventStartTime);
            TextView eventEndTime = (TextView) rowView.findViewById(R.id.eventEndTime);
            eventTitle.setText(title);
            if (location != null && location.length() > 0)
            {
                eventLocation.setVisibility(View.VISIBLE);
                eventLocation.setText(location);
            } else
            {
                eventLocation.setVisibility(View.GONE);
            }
            String dayOfWeek = mDayOfWeekFormat.format(endTime);
            eventDayOfWeek.setText(dayOfWeek);
            String timeAsString = mSimpleDateFormat.format(new Date(startTime));
            eventStartTime.setText(timeAsString + " - ");
            timeAsString = mSimpleDateFormat.format(new Date(endTime));
            eventEndTime.setText(timeAsString);
        }

        public int getEventColor(String id)
        {

            final String eventId = id;

            if (mEventMap != null)
            {
                Gson converter = new Gson();
                try
                {
                    mCurrentEvent = converter.fromJson(mEventMap.getString(eventId), CalendarEventRecordingTrigger.class);
                    mEventData[0] = mCurrentEvent.getStressLevel();
                    mEventData[1] = mCurrentEvent.getNoiseLevel();
                    mEventData[2] = mCurrentEvent.getMovementLevel();

                } catch (org.json.JSONException j)
                {
                    mCurrentEvent = null;
                }
            }

            if (mCurrentEvent == null)
                return 0;
            return mCurrentEvent.getStressLevel();
        }

        public void setRecentDate(String recentDate)
        {
            mRecentDate = recentDate;
        }

        public String getRecentDate()
        {
            return mRecentDate;
        }

        public CardView getCurrentCard()
        {
            return mCurrentCard;
        }

        public void setCurrentCard(CardView cardView)
        {
            mCurrentCard = cardView;
        }

        public LinearLayout getInnerCardLayout()
        {

            return mInnerCardLayout;
        }

        public void setInnerCardLayout(LinearLayout view)
        {
            mInnerCardLayout = view;
        }
    }
}


