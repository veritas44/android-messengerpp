package org.solovyev.android.messenger.messages;

import android.app.Activity;
import android.content.ClipboardManager;
import android.content.Context;
import android.text.Html;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import org.solovyev.android.list.ListItemOnClickData;
import org.solovyev.android.list.SimpleMenuOnClick;
import org.solovyev.android.menu.LabeledMenuItem;
import org.solovyev.android.messenger.chats.Chat;
import org.solovyev.android.messenger.chats.ChatEvent;
import org.solovyev.android.messenger.chats.ChatEventType;
import org.solovyev.android.messenger.core.R;
import org.solovyev.android.messenger.users.User;
import org.solovyev.android.messenger.view.AbstractMessengerListItem;
import org.solovyev.android.messenger.view.ViewAwareTag;

import javax.annotation.Nonnull;
import java.util.Arrays;

import static android.content.ClipData.newPlainText;
import static org.solovyev.android.messenger.App.getEventManager;
import static org.solovyev.android.messenger.chats.ChatUiEventType.chat_message_read;

/**
 * User: serso
 * Date: 6/10/12
 * Time: 11:25 PM
 */
public final class MessageListItem extends AbstractMessengerListItem<ChatMessage> /*, ChatEventListener*/ {

	@Nonnull
	private static final String TAG_PREFIX = "message_list_item_";

	@Nonnull
	private Chat chat;

	private final boolean userMessage;

	@Nonnull
	private final MessageListItemStyle style;

	private MessageListItem(@Nonnull Chat chat,
							@Nonnull ChatMessage message,
							boolean userMessage,
							@Nonnull MessageListItemStyle style) {
		super(TAG_PREFIX, message, R.layout.mpp_list_item_message, false);
		this.chat = chat;
		this.userMessage = userMessage;
		this.style = style;
	}

	@Nonnull
	public static MessageListItem newInstance(@Nonnull User user, @Nonnull Chat chat, @Nonnull ChatMessage message, @Nonnull MessageListItemStyle style) {
		final boolean userMessage = user.getEntity().equals(message.getAuthor());
		return new MessageListItem(chat, message, userMessage, style);
	}

	@Override
	public OnClickAction getOnClickAction() {
		return new SimpleMenuOnClick<MessageListItem>(Arrays.<LabeledMenuItem<ListItemOnClickData<MessageListItem>>>asList(MenuItems.values()), this, "message-context-menu");
	}

	@Override
	public OnClickAction getOnLongClickAction() {
		return null;
	}

	@Nonnull
	@Override
	protected CharSequence getDisplayName(@Nonnull ChatMessage data, @Nonnull Context context) {
		return data.getBody();
	}

	@Override
	protected void fillView(@Nonnull ChatMessage message, @Nonnull Context context, @Nonnull ViewAwareTag viewTag) {
		final ViewGroup messageLayout = viewTag.getViewById(R.id.mpp_li_message_linearlayout);

		final TextView messageText = viewTag.getViewById(R.id.mpp_li_message_body_textview);
		messageText.setText(Html.fromHtml(message.getBody()));

		final TextView messageDate = viewTag.getViewById(R.id.mpp_li_message_date_textview);
		messageDate.setText(Messages.getMessageTime(message));

		final ImageView messageIcon = viewTag.getViewById(R.id.mpp_li_message_icon_imageview);
		MessageBubbleViews.setMessageBubbleMessageIcon(context, message, messageIcon);

		final View root = viewTag.getView();

		MessageBubbleViews.fillMessageBubbleViews(context, root, messageLayout, messageText, messageDate, userMessage, false, style);

		if (!message.isRead()) {
			final ChatMessage readMessage = message.cloneRead();
			setData(readMessage);
			getEventManager(context).fire(chat_message_read.newEvent(chat, readMessage));
		}
	}

	/*@Override*/
	public void onEvent(@Nonnull ChatEvent event) {
		if (ChatEventType.message_changed.isEvent(event.getType(), event.getChat(), chat)) {
			if (getData().equals(event.getData())) {
				setData(event.getDataAsChatMessage());
			}
		}
	}

	public static final class Comparator implements java.util.Comparator<MessageListItem> {

		@Nonnull
		private static final Comparator instance = new Comparator();

		private Comparator() {
		}

		@Nonnull
		public static Comparator getInstance() {
			return instance;
		}

		@Override
		public int compare(@Nonnull MessageListItem lhs, @Nonnull MessageListItem rhs) {
			return lhs.getData().getSendDate().compareTo(rhs.getData().getSendDate());
		}
	}

	private static enum MenuItems implements LabeledMenuItem<ListItemOnClickData<MessageListItem>> {

		copy(R.string.mpp_copy) {
			@Override
			public void onClick(@Nonnull ListItemOnClickData<MessageListItem> data, @Nonnull Context context) {
				final ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Activity.CLIPBOARD_SERVICE);

				final MessageListItem messageListItem = data.getDataObject();
				clipboard.setPrimaryClip(newPlainText(null, messageListItem.getData().getBody()));

				Toast.makeText(context, context.getString(R.string.mpp_message_copied_to_clipboard), Toast.LENGTH_SHORT).show();
			}
		},

		quote(R.string.mpp_quote) {

			@Override
			public void onClick(@Nonnull ListItemOnClickData<MessageListItem> data, @Nonnull Context context) {
				getEventManager(context).fire(MessageUiEventType.quote.newEvent(data.getDataObject().getMessage()));
			}
		};

		private int captionResId;

		private MenuItems(int captionResId) {
			this.captionResId = captionResId;
		}

		@Nonnull
		@Override
		public String getCaption(@Nonnull Context context) {
			return context.getString(captionResId);
		}
	}

	@Nonnull
	public ChatMessage getMessage() {
		return getData();
	}
}
