package com.daprlabs.cardstack;

import android.animation.Animator;
import android.support.v4.view.ViewCompat;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewPropertyAnimator;
import android.view.animation.OvershootInterpolator;

/**
 * Created by aaron on 4/12/2015.
 */
public class SwipeListener implements View.OnTouchListener, View.OnClickListener {

    private float ROTATION_DEGREES = 15f;
    private float initialX;
    private float initialY;

    private int mActivePointerId;
    private float initialXPress;
    private float initialYPress;
    private ViewGroup parent;
    private float parentWidth;

    private View card;
    SwipeCallback callback;
    private boolean deactivated;


    public SwipeListener(View card, SwipeCallback callback, float initialX, float initialY) {
        this.card = card;
        this.initialX = initialX;
        this.initialY = initialY;
        this.callback = callback;
        this.parent = (ViewGroup) card.getParent();
        this.parentWidth = parent.getWidth();
        this.card.setOnClickListener(this);
    }


    public SwipeListener(View card, final SwipeCallback callback, float initialX, float initialY, float rotation) {
        this.card = card;
        this.initialX = initialX;
        this.initialY = initialY;
        this.callback = callback;
        this.parent = (ViewGroup) card.getParent();
        this.parentWidth = parent.getWidth();
        this.ROTATION_DEGREES = rotation;
        this.card.setOnClickListener(this);
    }


    private boolean click = true;

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (deactivated) return false;
        switch (event.getAction() & MotionEvent.ACTION_MASK) {

            case MotionEvent.ACTION_DOWN:
                click = true;
                //gesture has begun
                float x;
                float y;

                mActivePointerId = event.getPointerId(0);

                x = event.getX();
                y = event.getY();

                initialXPress = x;
                initialYPress = y;
                break;

            case MotionEvent.ACTION_MOVE:
                //gesture is in progress
                click = false;
                final int pointerIndex = event.findPointerIndex(mActivePointerId);
                final float xMove = event.getX(pointerIndex);
                final float yMove = event.getY(pointerIndex);

                //calculate distance moved
                final float dx = xMove - initialXPress;
                final float dy = yMove - initialYPress;

                //calc rotation here

                float posX = card.getX() + dx;
                float posY = card.getY() + dy;

                card.setX(posX);
                card.setY(posY);
                //card.setRotation

                float distobjectX = posX - initialX;
                float rotation = ROTATION_DEGREES * 2.f * distobjectX / parentWidth;
                card.setRotation(rotation);
                break;

            case MotionEvent.ACTION_UP:
                //gesture has finished
                //check to see if card has moved beyond the left or right bounds or reset
                //card position
                checkCardForEvent();
                //check if this is a click event and then perform a click
                //this is a workaround, android doesn't play well with multiple listeners
                if (click) v.performClick();
                break;

            default:
                return false;
        }
        return true;
    }

    private void checkCardForEvent() {

        if (cardBeyondLeftBorder()) {
            animateOffScreenLeft().setListener(new Animator.AnimatorListener() {


                @Override
                public void onAnimationStart(Animator animation) {

                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    callback.cardSwipedLeft();
                }

                @Override
                public void onAnimationCancel(Animator animation) {

                }

                @Override
                public void onAnimationRepeat(Animator animation) {

                }
            });
            this.deactivated = true;
        } else if (cardBeyondRightBorder()) {
            animateOffScreenRight().setListener(new Animator.AnimatorListener() {


                @Override
                public void onAnimationStart(Animator animation) {

                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    callback.cardSwipedRight();
                }

                @Override
                public void onAnimationCancel(Animator animation) {

                }

                @Override
                public void onAnimationRepeat(Animator animation) {

                }
            });
            this.deactivated = true;
        } else {
            resetCardPosition();
        }
    }

    private boolean cardBeyondLeftBorder() {
        //check if cards middle is beyond the left quarter of the screen
        return (card.getX() + (card.getWidth() / 2) < (parent.getWidth() / 4.f));
    }

    private boolean cardBeyondRightBorder() {
        //check if card middle is beyond the right quarter of the screen
        return (card.getX() + (card.getWidth() / 2) > ((parent.getWidth() / 4.f) * 3));
    }

    private ViewPropertyAnimator resetCardPosition() {
        return card.animate()
                .setDuration(200)
                .setInterpolator(new OvershootInterpolator(1.5f))
                .x(initialX)
                .y(initialY)
                .rotation(0);
    }

    private ViewPropertyAnimator animateOffScreenLeft() {
        return card.animate()
                .setDuration(150)
                .x(-(parent.getWidth()))
                .y(0)
                .rotation(-30);
    }

    private ViewPropertyAnimator animateOffScreenRight() {
        return card.animate()
                .setDuration(150)
                .x(parent.getWidth())
                .y(0)
                .rotation(30);
    }

    @Override
    public void onClick(View v) {
        callback.cardClicked();
    }


    public interface SwipeCallback {
        void cardSwipedLeft();

        void cardSwipedRight();

        void cardClicked();
    }
}
