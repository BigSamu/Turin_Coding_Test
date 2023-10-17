package integrations.turnitin.com.membersearcher.service;

import java.util.concurrent.CompletableFuture;

import integrations.turnitin.com.membersearcher.client.MembershipBackendClient;
import integrations.turnitin.com.membersearcher.model.MembershipList;
import integrations.turnitin.com.membersearcher.model.UserList;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MembershipService {
	@Autowired
	private MembershipBackendClient membershipBackendClient;

	/**
	 * Method to fetch all memberships with their associated user details included.
	 * This method calls out to the php-backend service and fetches all memberships,
	 * it then calls to fetch the user details for each user individually and
	 * associates them with their corresponding membership.
	 *
	 * @return A CompletableFuture containing a fully populated MembershipList object.
	 */
	public CompletableFuture<MembershipList> fetchAllMembershipsWithUsers() {
		return membershipBackendClient.fetchMemberships()
				.thenCompose(members -> {
					// Directly work with the single CompletableFuture for fetching users.
          return membershipBackendClient.fetchUsers()
            .thenApply(userList -> {
              // Once the user list is available, we set the user for each member.
              members.getMemberships().forEach(member ->
                  userList.getUsers().stream()
                      .filter(user -> user.getId().equals(member.getUserId()))
                      .findFirst()
                      .ifPresent(member::setUser) // Set the user for the member if a match is found.
              );

              return members; // Return the updated memberships.
          });
				});
	}
}
