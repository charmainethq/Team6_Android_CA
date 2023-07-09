package com.iss;

import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.io.File;
import java.util.List;

public class CardsAdapter extends RecyclerView.Adapter<CardsAdapter.CardViewHolder> {
    private List<Card> cards;
    private Context context;

    public CardsAdapter(List<Card> cards, Context context) {
        this.cards = cards;
        this.context = context;
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


    public class CardViewHolder extends RecyclerView.ViewHolder {
        private ImageView cardImage;

        public CardViewHolder(View itemView) {
            super(itemView);
            cardImage = itemView.findViewById(R.id.card_image);
        }

        //TODO:
        // 1. if you click too fast it acts kinda funny. implement a delay if 2 cards are flipped?
        // 2. stuff doesn't persist eg orientation change score and time will die
        public void bind(final Card card, final int position) {
            // Show image only if card has been flipped or is matched.
            if (card.getFlipped() || card.getMatched()) {
                Glide.with(context)
                        .load(new File(card.getImagePath()))
                        .into(cardImage);
            } else {
                cardImage.setImageResource(R.drawable.back_image);
            }

            cardImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // Start the game when the first card is clicked
                    if (!((GameActivity) cardImage.getContext()).isGameStarted) {
                        ((GameActivity) cardImage.getContext()).startTimer();
                    }

                    // Handle first click on unflipped card
                    if (!card.getFlipped() && !card.getMatched() && GameActivity.firstCard == null) {
                        // Set the first card and flip it
                        GameActivity.firstCard = card;
                        card.setFlipped(true);

                        notifyDataSetChanged();
                    }

                    // Handle second click on unflipped card
                    else if (!card.getFlipped() && !card.getMatched() && GameActivity.firstCard != null) {
                        // Immediately flip the second card
                        card.setFlipped(true);
                        notifyDataSetChanged(); //TODO: not sure if this is the best option


                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                // Match found, set to matched and update the score
                                if (GameActivity.firstCard != null && GameActivity.firstCard.getImagePath().equals(card.getImagePath())) {
                                    GameActivity.firstCard.setMatched(true);
                                    card.setMatched(true);
                                    ((GameActivity) cardImage.getContext()).updateScore();
                                }

                                // No match found, flip it back
                                else {
                                    // null handler
                                    if (GameActivity.firstCard != null) {
                                        GameActivity.firstCard.setFlipped(false);
                                    }
                                    card.setFlipped(false);
                                }
                                GameActivity.firstCard = null;
                                notifyDataSetChanged();
                            }
                        }, 1000); // Adjust the delay time as needed (e.g., 1000 milliseconds = 1 second)
                    }
                }
            });
        }
    }
}
