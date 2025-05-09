package com.joshrap.liteweight.adapters;

import android.content.Context;
import android.text.SpannableString;
import android.text.style.UnderlineSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.joshrap.liteweight.R;
import com.joshrap.liteweight.interfaces.LinkCallbacks;
import com.joshrap.liteweight.models.user.Link;

import java.util.List;

public class SaveExerciseLinkAdapter extends RecyclerView.Adapter<SaveExerciseLinkAdapter.ViewHolder> {
    static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView linkTv;
        final ImageButton deleteLinkBtn;

        ViewHolder(View itemView) {
            super(itemView);
            linkTv = itemView.findViewById(R.id.link_tv);
            deleteLinkBtn = itemView.findViewById(R.id.delete_link_icon_btn);
        }
    }

    private final List<Link> links;
    private final LinkCallbacks linkCallbacks;

    public SaveExerciseLinkAdapter(List<Link> links, LinkCallbacks linkCallbacks) {
        this.links = links;
        this.linkCallbacks = linkCallbacks;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        View focusView = inflater.inflate(R.layout.row_save_exercise_link, parent, false);
        return new ViewHolder(focusView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position, @NonNull List<Object> payloads) {
        if (!payloads.isEmpty()) {
            setViews(holder, links.get(position), position);
        } else {
            super.onBindViewHolder(holder, position, payloads);
        }
    }


    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Link link = links.get(position);
        setViews(holder, link, position);
    }

    private void setViews(ViewHolder holder, Link link, int position) {
        TextView linkTv = holder.linkTv;
        ImageButton deleteLinkBtn = holder.deleteLinkBtn;
        String label = link.getUrl();
        if (link.getLabel() != null && !link.getLabel().isEmpty()) {
            label = link.getLabel();
        }

        // underline text
        SpannableString content = new SpannableString(label);
        content.setSpan(new UnderlineSpan(), 0, content.length(), 0);
        linkTv.setText(content);

        linkTv.setOnClickListener(v -> linkCallbacks.onClick(link, position));
        deleteLinkBtn.setOnClickListener(v -> linkCallbacks.onClear(link, position));
    }

    @Override
    public int getItemCount() {
        return links.size();
    }
}
