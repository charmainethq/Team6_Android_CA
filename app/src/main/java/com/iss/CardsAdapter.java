package com.iss;

import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class CardsAdapter extends RecyclerView.Adapter<CardsAdapter.CardViewHolder> {
    private List<MainActivity.Card> cards;

    public CardsAdapter(List<MainActivity.Card> cards) {
        this.cards = cards;
    }

    @NonNull
    @Override
    public CardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_card, parent, false);
        return new CardViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CardViewHolder holder, int position) {
        holder.bind(cards.get(position), position);
    }

    @Override
    public int getItemCount() {
        return cards.size();
    }

    //TODO: introduce delay after 2 unmatched cards flip?
    public class CardViewHolder extends RecyclerView.ViewHolder {
        private ImageView cardImage;

        public CardViewHolder(View itemView) {
            super(itemView);
            cardImage = itemView.findViewById(R.id.card_image);
        }

        public void bind(final MainActivity.Card card, final int position) {
            // Show image only if card has been flipped or is matched.
            if (card.isFlipped || card.isMatched) {
                cardImage.setImageResource(card.image);
            } else {
                cardImage.setImageResource(R.drawable.back_image);
            }

            cardImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    // Handle first click on unflipped card
                    if (!card.isFlipped && !card.isMatched && MainActivity.selectedCard == null) {
                        // First click on a placeholder
                        MainActivity.selectedCard = card;
                        card.isFlipped = true;
                        notifyDataSetChanged();
                    }

                    // Handle second click on unflipped card
                    else if (!card.isFlipped && !card.isMatched && MainActivity.selectedCard != null) {
                        // Second click on a different placeholder
                        card.isFlipped = true;
                        notifyDataSetChanged();

                        // Introduce delay so it doesn't immediately flip
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                // Match found, set to matched
                                if (MainActivity.selectedCard != null && MainActivity.selectedCard.image == card.image) {
                                    MainActivity.selectedCard.isMatched = true;
                                    card.isMatched = true;
                                }

                                // No match found, flip it back
                                else {
                                    // null handler
                                    if (MainActivity.selectedCard != null) {
                                        MainActivity.selectedCard.isFlipped = false;
                                    }
                                    card.isFlipped = false;
                                }
                                MainActivity.selectedCard = null;
                                notifyDataSetChanged();
                            }
                        }, 1000); // Adjust the delay time as needed (e.g., 1000 milliseconds = 1 second)
                    }
                }
            });
        }


    }
}
