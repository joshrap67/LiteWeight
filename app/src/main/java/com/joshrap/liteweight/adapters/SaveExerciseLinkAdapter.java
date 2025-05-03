package com.joshrap.liteweight.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.joshrap.liteweight.R;
import com.joshrap.liteweight.interfaces.ClearLinkCallback;
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
    private final ClearLinkCallback clearLinkCallback;

    public SaveExerciseLinkAdapter(List<Link> links, ClearLinkCallback clearLinkCallback) {
        this.links = links;
        this.clearLinkCallback = clearLinkCallback;
    }

    @NonNull
    @Override
    public SaveExerciseLinkAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        View focusView = inflater.inflate(R.layout.row_save_exercise_link, parent, false);
        return new ViewHolder(focusView);
    }

    @Override
    public void onBindViewHolder(SaveExerciseLinkAdapter.ViewHolder holder, int position) {
        Link link = links.get(position);
        TextView linkTv = holder.linkTv;
        ImageButton deleteLinkBtn = holder.deleteLinkBtn;
        String label = link.getUrl();
        if (link.getLabel() != null && !link.getLabel().isEmpty()) {
            label = link.getLabel();
        }
        // TODO style link as hyperlink
        linkTv.setText(label);
        deleteLinkBtn.setOnClickListener(v -> clearLinkCallback.onClear(link));
    }

    @Override
    public int getItemCount() {
        return links.size();
    }
}
