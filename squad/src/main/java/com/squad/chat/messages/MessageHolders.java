package com.squad.chat.messages;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.LayoutRes;

import com.squad.chat.R;
import com.squad.chat.commons.ImageLoader;
import com.squad.chat.commons.ViewHolder;
import com.squad.chat.commons.models.IMessage;
import com.squad.chat.commons.models.MessageContentType;
import com.squad.chat.utils.DateFormatter;

import java.lang.reflect.Constructor;
import java.util.Date;


@SuppressWarnings("WeakerAccess")
public class MessageHolders {

    private static final int VIEW_TYPE_DATE_HEADER = 130;
    private static final int VIEW_TYPE_TEXT_MESSAGE = 131;
    private static final int VIEW_TYPE_IMAGE_MESSAGE = 132;

    private Class<? extends ViewHolder<Date>> dateHeaderHolder;

    private HolderConfig<IMessage> incomingTextConfig;
    private HolderConfig<IMessage> outcomingTextConfig;
    private HolderConfig<MessageContentType.Image> incomingImageConfig;
    private HolderConfig<MessageContentType.Image> outcomingImageConfig;

    public MessageHolders() {
        this.dateHeaderHolder = DefaultDateHeaderViewHolder.class;

        this.incomingTextConfig = new HolderConfig<>(DefaultIncomingTextMessageViewHolder.class, R.layout.item_incoming_text_message);
        this.outcomingTextConfig = new HolderConfig<>(DefaultOutcomingTextMessageViewHolder.class, R.layout.item_outcoming_text_message);
        this.incomingImageConfig = new HolderConfig<>(DefaultIncomingImageMessageViewHolder.class, R.layout.item_incoming_image_message);
        this.outcomingImageConfig = new HolderConfig<>(DefaultOutcomingImageMessageViewHolder.class, R.layout.item_outcoming_image_message);
    }


    protected ViewHolder getHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case VIEW_TYPE_DATE_HEADER:
                return getHolder(parent, R.layout.item_date_header, dateHeaderHolder, null);
            case VIEW_TYPE_TEXT_MESSAGE:
                return getHolder(parent, incomingTextConfig);
            case -VIEW_TYPE_TEXT_MESSAGE:
                return getHolder(parent, outcomingTextConfig);
            case VIEW_TYPE_IMAGE_MESSAGE:
                return getHolder(parent, incomingImageConfig);
            case -VIEW_TYPE_IMAGE_MESSAGE:
                return getHolder(parent, outcomingImageConfig);
            default:
                throw new IllegalStateException("View not recognized");
        }
    }


    @SuppressWarnings("unchecked")
    protected void bind(final ViewHolder holder, final Object item, boolean isSelected,
                        final ImageLoader imageLoader,
                        final View.OnClickListener onMessageClickListener,
                        final View.OnLongClickListener onMessageLongClickListener,
                        final DateFormatter.Formatter dateHeadersFormatter) {

        if (item instanceof IMessage) {
            ((MessageHolders.BaseMessageViewHolder) holder).isSelected = isSelected;
            ((MessageHolders.BaseMessageViewHolder) holder).imageLoader = imageLoader;
            holder.itemView.setOnLongClickListener(onMessageLongClickListener);
            holder.itemView.setOnClickListener(onMessageClickListener);
        } else if (item instanceof Date) {
            ((MessageHolders.DefaultDateHeaderViewHolder) holder).dateHeadersFormatter = dateHeadersFormatter;
        }

        holder.onBind(item);
    }

    protected int getViewType(Object item, String senderId) {
        boolean isOutcoming = false;
        int viewType;

        if (item instanceof IMessage) {
            IMessage message = (IMessage) item;
            isOutcoming = message.getUser().getId().contentEquals(senderId);
            viewType = getContentViewType(message);

        } else viewType = VIEW_TYPE_DATE_HEADER;

        return isOutcoming ? viewType * -1 : viewType;
    }

    private ViewHolder getHolder(ViewGroup parent, HolderConfig holderConfig) {
        return getHolder(parent, holderConfig.layout, holderConfig.holder, holderConfig.payload);
    }

    private <HOLDER extends ViewHolder> ViewHolder getHolder(ViewGroup parent, @LayoutRes int layout, Class<HOLDER> holderClass, Object payload) {

        View v = LayoutInflater.from(parent.getContext()).inflate(layout, parent, false);
        try {
            Constructor<HOLDER> constructor;
            HOLDER holder;
            try {
                constructor = holderClass.getDeclaredConstructor(View.class, Object.class);
                constructor.setAccessible(true);
                holder = constructor.newInstance(v, payload);
            } catch (NoSuchMethodException e) {
                constructor = holderClass.getDeclaredConstructor(View.class);
                constructor.setAccessible(true);
                holder = constructor.newInstance(v);
            }
            return holder;
        } catch (Exception e) {
            throw new UnsupportedOperationException("unknown holder for viewtype.", e);
        }
    }

    private int getContentViewType(IMessage message) {
        if (message instanceof MessageContentType.Image
                && ((MessageContentType.Image) message).getImageUrl() != null) {
            return VIEW_TYPE_IMAGE_MESSAGE;
        }

        return VIEW_TYPE_TEXT_MESSAGE;
    }


    /**
     * The base class for view holders for incoming and outcoming message.
     * You can extend it to create your own holder in conjuction with custom layout or even using default layout.
     */
    public static abstract class BaseMessageViewHolder<MESSAGE extends IMessage> extends ViewHolder<MESSAGE> {

        /**
         * Callback for implementing images loading in message list
         */
        protected ImageLoader imageLoader;
        boolean isSelected;

        public BaseMessageViewHolder(View itemView) {
            super(itemView);
        }

        /**
         * Returns whether is item selected
         *
         * @return weather is item selected.
         */
        public boolean isSelected() {
            return isSelected;
        }

    }

    /**
     * Default view holder implementation for incoming text message
     */
    public static class IncomingTextMessageViewHolder<MESSAGE extends IMessage>
            extends BaseIncomingMessageViewHolder<MESSAGE> {

        protected View imageOverlay;
        protected TextView text;


        public IncomingTextMessageViewHolder(View itemView) {
            super(itemView);
            init(itemView);
        }

        @Override
        public void onBind(MESSAGE message) {
            super.onBind(message);
            if (text != null) {
                text.setText(message.getText());
            }
            if (imageOverlay != null) {
                imageOverlay.setSelected(isSelected());
            }
        }


        private void init(View itemView) {
            imageOverlay = itemView.findViewById(R.id.imageOverlay);
            text = itemView.findViewById(R.id.messageText);
        }
    }

    /**
     * Default view holder implementation for outcoming text message
     */
    public static class OutcomingTextMessageViewHolder<MESSAGE extends IMessage>
            extends BaseMessageViewHolder<MESSAGE> {

        protected View imageOverlay;
        protected TextView text;

        public OutcomingTextMessageViewHolder(View itemView) {
            super(itemView);
            init(itemView);
        }

        @Override
        public void onBind(MESSAGE message) {
            if (imageOverlay != null) {
                imageOverlay.setSelected(isSelected());
            }

            if (text != null) {
                text.setText(message.getText());
            }
        }

        private void init(View itemView) {
            imageOverlay = itemView.findViewById(R.id.imageOverlay);
            text = itemView.findViewById(R.id.messageText);
        }
    }

    /**
     * Default view holder implementation for incoming image message
     */
    public static class IncomingImageMessageViewHolder<MESSAGE extends MessageContentType.Image>
            extends BaseIncomingMessageViewHolder<MESSAGE> {

        protected ImageView image;
        protected View imageOverlay;

        public IncomingImageMessageViewHolder(View itemView) {
            super(itemView);
            init(itemView);
        }

        @Override
        public void onBind(MESSAGE message) {
            super.onBind(message);
            if (image != null && imageLoader != null) {
                imageLoader.loadImage(image, message.getImageUrl());
            }

            if (imageOverlay != null) {
                imageOverlay.setSelected(isSelected());
            }
        }


        /**
         * Override this method to have ability to pass custom data in ImageLoader for loading image(not avatar).
         */
        protected Object getPayloadForImageLoader() {
            return null;
        }

        private void init(View itemView) {
            image = itemView.findViewById(R.id.image);
            imageOverlay = itemView.findViewById(R.id.imageOverlay);

        }
    }

    /**
     * Default view holder implementation for outcoming image message
     */
    public static class OutcomingImageMessageViewHolder<MESSAGE extends MessageContentType.Image>
            extends BaseMessageViewHolder<MESSAGE> {

        protected ImageView image;
        protected View imageOverlay;

        public OutcomingImageMessageViewHolder(View itemView) {
            super(itemView);
            init(itemView);
        }

        @Override
        public void onBind(MESSAGE message) {
            if (image != null && imageLoader != null) {
                imageLoader.loadImage(image, message.getImageUrl());
            }

            if (imageOverlay != null) {
                imageOverlay.setSelected(isSelected());
            }
        }


        private void init(View itemView) {
            image = itemView.findViewById(R.id.image);
            imageOverlay = itemView.findViewById(R.id.imageOverlay);
        }
    }

    /**
     * Default view holder implementation for date header
     */
    public static class DefaultDateHeaderViewHolder extends ViewHolder<Date> {

        protected TextView text;
        protected String dateFormat;
        protected DateFormatter.Formatter dateHeadersFormatter;

        public DefaultDateHeaderViewHolder(View itemView) {
            super(itemView);
            text = itemView.findViewById(R.id.messageText);
        }

        @Override
        public void onBind(Date date) {
            if (text != null) {
                String formattedDate = null;
                if (dateHeadersFormatter != null) formattedDate = dateHeadersFormatter.format(date);
                text.setText(formattedDate == null ? DateFormatter.Companion.format(date, dateFormat) : formattedDate);
            }
        }

    }

    /**
     * Base view holder for incoming message
     */
    public abstract static class BaseIncomingMessageViewHolder<MESSAGE extends IMessage>
            extends BaseMessageViewHolder<MESSAGE> {

        protected ImageView userAvatar;


        public BaseIncomingMessageViewHolder(View itemView) {
            super(itemView);
            init(itemView);
        }

        @Override
        public void onBind(MESSAGE message) {
            if (userAvatar != null) {
                boolean isAvatarExists = imageLoader != null && !message.getUser().getAvatar().isEmpty();

                userAvatar.setVisibility(isAvatarExists ? View.VISIBLE : View.GONE);
                if (isAvatarExists) {
                    imageLoader.loadImage(userAvatar, message.getUser().getAvatar());
                }
            }
        }


        private void init(View itemView) {
            userAvatar = itemView.findViewById(R.id.messageUserAvatar);
        }
    }

    /*
     * DEFAULTS
     * */

    private static class DefaultIncomingTextMessageViewHolder
            extends IncomingTextMessageViewHolder<IMessage> {

        public DefaultIncomingTextMessageViewHolder(View itemView) {
            super(itemView);
        }
    }

    private static class DefaultOutcomingTextMessageViewHolder
            extends OutcomingTextMessageViewHolder<IMessage> {

        public DefaultOutcomingTextMessageViewHolder(View itemView) {
            super(itemView);
        }
    }

    private static class DefaultIncomingImageMessageViewHolder
            extends IncomingImageMessageViewHolder<MessageContentType.Image> {

        public DefaultIncomingImageMessageViewHolder(View itemView) {
            super(itemView);
        }
    }

    private static class DefaultOutcomingImageMessageViewHolder
            extends OutcomingImageMessageViewHolder<MessageContentType.Image> {

        public DefaultOutcomingImageMessageViewHolder(View itemView) {
            super(itemView);
        }
    }

    private class HolderConfig<T extends IMessage> {

        protected Class<? extends BaseMessageViewHolder<? extends T>> holder;
        protected int layout;
        protected Object payload;

        HolderConfig(Class<? extends BaseMessageViewHolder<? extends T>> holder, int layout) {
            this.holder = holder;
            this.layout = layout;
        }

    }
}
