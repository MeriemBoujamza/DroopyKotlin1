package ma.ensaf.veryempty.adapters.viewholders;

import androidx.recyclerview.widget.RecyclerView;

import ma.ensaf.veryempty.databinding.ItemDonorsRowBinding;

public class RowViewHolder extends RecyclerView.ViewHolder {

    public ItemDonorsRowBinding binding;

    public RowViewHolder(final ItemDonorsRowBinding itemBinding) {
        super(itemBinding.getRoot());
        this.binding = itemBinding;
    }
}
