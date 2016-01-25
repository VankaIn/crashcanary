package com.github.vankain.crashcanary_library.ui;

import android.content.Context;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.github.vankain.crashcanary_library.R;
import com.github.vankain.crashcanary_library.log.Crash;

import java.util.Arrays;

/**
 * @author yifan.zhai on 15/9/27.
 */
final class CrashDetailAdapter extends BaseAdapter {

    private static final int TOP_ROW = 0;
    private static final int NORMAL_ROW = 1;

    private boolean[] mFoldings = new boolean[0];

    private Crash mCrash;

    private static final int POSITION_BASIC = 1;
    private static final int POSITION_TIME = 2;
    private static final int POSITION_CAUSE = 3;
    private static final int POSITION_DETAIL = 4;

    private static final int ITEM_COUNT = 5;

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Context context = parent.getContext();
        if (getItemViewType(position) == TOP_ROW) {
            if (convertView == null) {
                convertView =
                        LayoutInflater.from(context).inflate(R.layout.crash_canary_ref_top_row, parent, false);
            }
            TextView textView = findById(convertView, R.id.__leak_canary_row_text);
            textView.setText(context.getPackageName());
        } else {
            if (convertView == null) {
                convertView =
                        LayoutInflater.from(context).inflate(R.layout.crash_canary_ref_row, parent, false);
            }
            TextView textView = findById(convertView, R.id.__leak_canary_row_text);

            String element = getItem(position);
            String htmlString = elementToHtmlString(element, position, mFoldings[position]);
            textView.setText(Html.fromHtml(htmlString));

            DisplayLeakConnectorView connectorView = findById(convertView, R.id.__leak_canary_row_connector);
            connectorView.setType(connectorViewType(position));

            MoreDetailsView moreDetailsView = findById(convertView, R.id.__leak_canary_row_more);
            moreDetailsView.setFolding(mFoldings[position]);
        }

        return convertView;
    }

    private DisplayLeakConnectorView.Type connectorViewType(int position) {
        return (position == 1) ? DisplayLeakConnectorView.Type.START : (
                (position == getCount() - 1) ? DisplayLeakConnectorView.Type.END :
                        DisplayLeakConnectorView.Type.NODE);
    }

    private String elementToHtmlString(String element, int position, boolean folding) {
        String htmlString = element.replaceAll(Crash.SEPARATOR, "<br>");
        switch (position) {
            case POSITION_BASIC:
                if (folding) {
                    htmlString = htmlString.substring(htmlString.indexOf(Crash.KEY_UID));
                }
                htmlString = String.format("<font color='#c48a47'>%s</font> ", htmlString);
                break;
            case POSITION_TIME:
                htmlString = String.format("<font color='#f3cf83'>%s</font> ", htmlString);
                break;
            case POSITION_CAUSE:
                htmlString = String.format("<font color='#998bb5'>%s</font> ", htmlString);
                break;
            case POSITION_DETAIL:
                if (folding) {
                    htmlString = htmlString.substring(htmlString.indexOf(Crash.KEY_CRASH_MESSAGE));
                }
                htmlString = String.format("<font color='#ffffff'>%s</font> ", htmlString);
                break;
            default:
                if (folding) {
                    htmlString = htmlString.substring(htmlString.indexOf(Crash.KEY_CRASH_MESSAGE));
                }
                htmlString = String.format("<font color='#ffffff'>%s</font> ", htmlString);
                break;
        }
        return htmlString;
    }

    public void update(Crash crash) {
        if (mCrash != null && crash.time.equals(mCrash.time)) {
            // Same data, nothing to change.
            return;
        }
        this.mCrash = crash;
        mFoldings = new boolean[ITEM_COUNT];
        Arrays.fill(mFoldings, false);
        mFoldings[1] = true;
        notifyDataSetChanged();
    }

    public void toggleRow(int position) {
        mFoldings[position] = !mFoldings[position];
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        if (mCrash == null) {
            return 0;
        }
        return ITEM_COUNT;
    }

    @Override
    public String getItem(int position) {
        if (getItemViewType(position) == TOP_ROW) {
            return null;
        }
        switch (position) {
            case POSITION_BASIC:
                return mCrash.getBasicString();
            case POSITION_TIME:
                return mCrash.getTimeString();
            case POSITION_CAUSE:
                return mCrash.getCauseString();
            case POSITION_DETAIL:
                return mCrash.getCrashMessageString();
            default:
                return mCrash.getCrashMessageString();
        }
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0) {
            return TOP_ROW;
        }
        return NORMAL_ROW;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @SuppressWarnings("unchecked")
    private static <T extends View> T findById(View view, int id) {
        return (T) view.findViewById(id);
    }
}

