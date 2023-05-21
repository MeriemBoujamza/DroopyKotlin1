package ma.ensaf.veryempty.adapters.viewholders;

import androidx.recyclerview.widget.RecyclerView;

import ma.ensaf.veryempty.databinding.ItemDonorsHeaderBinding;

public class HeaderViewHolder extends RecyclerView.ViewHolder {

    public ItemDonorsHeaderBinding binding;

    public HeaderViewHolder(final ItemDonorsHeaderBinding itemBinding) {
        super(itemBinding.getRoot());
        this.binding = itemBinding;
    }
}
