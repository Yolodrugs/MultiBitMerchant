package org.multibit.mbm.db.dao;

import com.google.common.base.Optional;
import org.multibit.mbm.db.dto.User;

import java.util.List;

public interface UserDao {

  /**
   * Attempt to locate the User
   *
   * @param id The ID
   *
   * @return A matching User
   */
  Optional<User> getById(Long id);

  /**
   * Attempt to locate the User
   *
   * @param openId The OpenId token
   *
   * @return A matching User
   */
  Optional<User> getByOpenId(String openId);

  /**
   * Attempt to locate the User by a UUID
   *
   * @param uuid The UUID that acts as a unique identifier when Open ID is not available
   *
   * @return A matching User
   */
  Optional<User> getByUUID(String uuid);

  /**
   * Attempt to locate the User by a UUID
   *
   * @param username The username
   * @param password The password (as provided by the security token)
   *
   * @return A matching User with Roles and Authorities initialised
   */
  Optional<User> getByCredentials(String username, String password);

  /**
   * Provide a paged list of all Users
   *
   * @param pageSize   the total record in one page
   * @param pageNumber the page number starts from 0
   */
  List<User> getAllByPage(final int pageSize, final int pageNumber);

  /**
   * Persist the given User
   *
   * @param User A User (either new or updated)
   *
   * @return The persisted User
   */
  User saveOrUpdate(User User);

  /**
   * <p>Force an immediate in-transaction flush</p>
   * <p>Normally, this is only used in test code but must be on the interface to ensure
   * that injection works as expected</p>
   */
  void flush();

}