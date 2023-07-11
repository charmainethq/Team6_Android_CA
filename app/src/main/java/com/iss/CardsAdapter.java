package com.iss;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;

import java.io.File;
import java.util.List;

public class CardsAdapter extends RecyclerView.Adapter<CardsAdapter.CardViewHolder> {
    private List<Card> cards;
    private RecyclerView recyclerView;
    private MediaPlayer clickSoundPlayer;
    private Context context;

    public CardsAdapter(List<Card> cards, RecyclerView recyclerView, Context context) {
        this.cards = cards;
        this.context = context;
        this.recyclerView = recyclerView;
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

        private void flipCard(final int position) {
            View view = itemView;
            AnimatorSet animatorSet = (AnimatorSet) AnimatorInflater.loadAnimator(
                    view.getContext(), R.animator.flip_forward);
            animatorSet.setTarget(view);
            animatorSet.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    // Update the card's flipped state after the animation completes
                    cards.get(position).setFlipped(true);
                     notifyDataSetChanged();

                }
            });
            animatorSet.start();
        }

        private void flipBackCard(final int position) {
            RecyclerView.ViewHolder viewHolder = recyclerView.findViewHolderForAdapterPosition(position);
            if (viewHolder == null) {
                return;
            }
            View view = viewHolder.itemView;
            AnimatorSet animatorSet = (AnimatorSet) AnimatorInflater.loadAnimator(
                    view.getContext(), R.animator.flip_backward);
            animatorSet.setTarget(view);
            animatorSet.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    cards.get(position).setFlipped(false);
                    notifyDataSetChanged();
                }
            });
            animatorSet.start();
        }

        public void bind(final Card card, final int position) {
            // Show image only if card has been flipped or is matched.
            if (card.getFlipped() || card.getMatched()) {
                Glide.with(context)
                        .load(new File(card.getImagePath()))
                        .listener(new RequestListener<Drawable>() {
                            @Override
                            public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                                return false;
                            }

                            @Override
                            public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                                // Only flip the card if it's not already flipped or matched
                                if (!card.getFlipped() && !card.getMatched()) {
                                    flipCard(position);
                                }
                                return false;
                            }
                        })
                        .into(cardImage);
            } else {
                cardImage.setImageResource(R.drawable.back_image);
            }

            cardImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // add click sound
                    clickSoundPlayer = MediaPlayer.create(itemView.getContext(), R.raw.smb_kick);
                    clickSoundPlayer.setVolume(2.5f, 2.5f);
                    clickSoundPlayer.start();
                    // Start the game when the first card is clicked
                    if (!((GameActivity) cardImage.getContext()).isGameStarted) {
                        ((GameActivity) cardImage.getContext()).startTimer();
                    }

                    // Handle first click on unflipped card
                    if (!card.getFlipped() && !card.getMatched() && GameActivity.firstCard == null) {
                        // Set the first card and flip it
                        GameActivity.firstCard = card;
                        flipCard(position);
                    }

                    // Handle second click on unflipped card
                    else if (!card.getFlipped() && !card.getMatched() && GameActivity.firstCard != null) {
                        // Immediately flip the second card
                        flipCard(position);

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

                                        flipBackCard(position);
                                        flipBackCard(cards.indexOf(GameActivity.firstCard));
                                    }
                                }
                                GameActivity.firstCard = null;
                            }
                        }, 600); // Adjust the delay time as needed (e.g., 1000 milliseconds = 1 second)
                    }
                }
            });
        }
    }
}


