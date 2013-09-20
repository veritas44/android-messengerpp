package org.solovyev.android.messenger.realms.vk;

import android.util.Log;
import org.solovyev.android.captcha.ResolvedCaptcha;
import org.solovyev.android.http.HttpTransactions;
import org.solovyev.android.messenger.entities.EntityImpl;
import org.solovyev.android.messenger.realms.AbstractAccountBuilder;
import org.solovyev.android.messenger.realms.AccountState;
import org.solovyev.android.messenger.realms.Account;
import org.solovyev.android.messenger.realms.RealmDef;
import org.solovyev.android.messenger.realms.vk.auth.JsonAuthResult;
import org.solovyev.android.messenger.realms.vk.auth.VkAuth;
import org.solovyev.android.messenger.realms.vk.users.VkUsersGetHttpTransaction;
import org.solovyev.android.messenger.security.InvalidCredentialsException;
import org.solovyev.android.messenger.users.User;
import org.solovyev.android.messenger.users.Users;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.util.List;

public class VkAccountBuilder extends AbstractAccountBuilder<VkAccountConfiguration> {

	protected VkAccountBuilder(@Nonnull RealmDef realmDef, @Nullable Account editedAccount, @Nonnull VkAccountConfiguration configuration) {
		super(realmDef, configuration, editedAccount);
	}

	@Nonnull
	@Override
	protected User getRealmUser(@Nonnull String realmId) {
		final String userId = getConfiguration().getUserId();
		final User defaultUser = Users.newEmptyUser(EntityImpl.newInstance(realmId, userId));

		User result;
		try {
			final List<User> users = HttpTransactions.execute(VkUsersGetHttpTransaction.newInstance(new VkAccount(realmId, getRealmDef(), defaultUser, getConfiguration(), AccountState.removed), userId, null));
			if (users.isEmpty()) {
				result = defaultUser;
			} else {
				result = users.get(0);
			}
		} catch (IOException e) {
			Log.e("VkRealmBuilder", e.getMessage(), e);
			result = defaultUser;
		}

		return result;
	}

	@Nonnull
	@Override
	protected Account newRealm(@Nonnull String id, @Nonnull User user, @Nonnull AccountState state) {
		return new VkAccount(id, getRealmDef(), user, getConfiguration(), state);
	}

	@Override
	public void connect() throws ConnectionException {
	}

	@Override
	public void disconnect() throws ConnectionException {
	}

	@Override
	public void loginUser(@Nullable ResolvedCaptcha resolvedCaptcha) throws InvalidCredentialsException {
		final VkAccountConfiguration configuration = getConfiguration();

		final JsonAuthResult result = VkAuth.doOauth2Authorization(configuration.getLogin(), configuration.getPassword());
		configuration.setAccessParameters(result.getAccessToken(), result.getUserId());
	}

}