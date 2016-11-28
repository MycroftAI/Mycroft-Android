package mycroft.ai.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import mycroft.ai.MycroftUtterances;
import mycroft.ai.R;

/**
 * Created by paul on 2016/06/22.
 */

public class MycroftAdapter extends RecyclerView.Adapter<MycroftAdapter.UtteranceViewHolder> {

    private List<MycroftUtterances> utteranceList;

    public MycroftAdapter(List<MycroftUtterances> utteranceList) {
        this.utteranceList = utteranceList;
    }

    @Override
    public int getItemCount() {
        return utteranceList.size();
    }

    @Override
    public void onBindViewHolder(UtteranceViewHolder utteranceViewHolder, int i) {
        MycroftUtterances ci = utteranceList.get(i);
        utteranceViewHolder.vUtterance.setText(ci.UTTERANCE_PREFIX + " " + ci.utterance);
    }

    @Override
    public UtteranceViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View itemView = LayoutInflater.
                from(viewGroup.getContext()).
                inflate(R.layout.card_layout, viewGroup, false);

        return new UtteranceViewHolder(itemView);
    }

    public static class UtteranceViewHolder extends RecyclerView.ViewHolder {

        protected TextView vUtterance;

        public UtteranceViewHolder(View v) {
            super(v);
            vUtterance = (TextView) v.findViewById(R.id.utterance);
        }
    }
}