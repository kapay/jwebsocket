//	---------------------------------------------------------------------------
//	jWebSocket - User Class
//	Copyright (c) 2010 jWebSocket.org, Alexander Schulze, Innotrade GmbH
//	---------------------------------------------------------------------------
//	This program is free software; you can redistribute it and/or modify it
//	under the terms of the GNU General Public License as published by the
//	Free Software Foundation; either version 3 of the License, or (at your
//	option) any later version.
//	This program is distributed in the hope that it will be useful, but WITHOUT
//	ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
//	FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
//	more details.
//	You should have received a copy of the GNU General Public License along
//	with this program; if not, see <http://www.gnu.org/licenses/>.
//	---------------------------------------------------------------------------
package org.jwebsocket.security;

import java.util.Properties;
import javolution.util.FastMap;
import org.apache.log4j.Logger;

/**
 * Implements a user with all its data fields, rights, roles and settings.
 * @author aschulze
 * @since 1.0
 */
public class User {

	private static Logger log = Logger.getLogger(User.class);
	/**
	 * The maximum number of login tries until the account gets locked.
	 * @since 1.0
	 */
	public static int MAX_PWD_FAIL_COUNT = 3;
	/**
	 * The state of the user is unknown. This state is used only as default
	 * when instantiating a new user. This value should not be saved.
	 * @since 1.0
	 */
	public static int ST_UNKNOWN = -1;
	/**
	 * The user is already registered but not activated.
	 * A user needs to get activated to get access to the system.
	 * @since 1.0
	 */
	public static int ST_REGISTERED = 0;
	/**
	 * The user is activated and has access to the system according to his 
	 * rights and roles.
	 * @since 1.0
	 */
	public static int ST_ACTIVE = 1;
	/**
	 * The user is (temporarily) inactive.
	 * He needs to get (re-)activated to get access to the system.
	 * @since 1.0
	 */
	public static int ST_INACTIVE = 2;
	/**
	 * The user is (temporarily) locked, eg due to too much logins 
	 * with wrong credentials.
	 * He needs to gets unlocked again to get access to the system.
	 * @since 1.0
	 */
	public static int ST_LOCKED = 3;
	/**
	 * The user is deleted, he can't log in and is not reachable for others.
	 * The row is kept in the database for reference purposes only and
	 * to keep the database consistent (eg for logs, journal or transactions).
	 * He can be activated again to get access to the system.
	 * @since 1.0
	 */
	public static int ST_DELETED = 4;
	private Integer userId = null;
	private String loginname = null;
	private String title = null;
	private String company = null;
	private String firstname = null;
	private String lastname = null;
	private String password = null;
	private Integer pwdFailCount = 0;
	private int status = ST_UNKNOWN;
	private String defaultLocale = null;
	private String city = null;
	private String address = null;
	private String zipcode = null;
	private String country_code = null;
	private String emailOffice = null;
	private String emailPrivate = null;
	private String phoneOffice = null;
	private String phonePrivate = null;
	private String phoneCell = null;
	private String faxOffice = null;
	private String faxPrivate = null;
	private String activationCode = null;
	private String securityQuestion = null;
	private String securityAnswer = null;
	private int sessionTimeout = 0;
	private Properties settings = new Properties();
	private FastMap roles = new FastMap();
	private FastMap rights = new FastMap();

	/**
	 *
	 * @return
	 */
	public Integer getUserId() {
		return userId;
	}

	/**
	 *
	 * @param userId
	 */
	public void setUserId(Integer userId) {
		this.userId = userId;
	}

	/**
	 *
	 * @return
	 */
	public String getLoginname() {
		return loginname;
	}

	/**
	 *
	 * @param loginName
	 */
	public void setLoginname(String loginName) {
		this.loginname = loginName;
	}

	/**
	 * @return the title
	 */
	public String getTitle() {
		return title;
	}

	/**
	 * @param title the title to set
	 */
	public void setTitle(String title) {
		this.title = title;
	}

	/**
	 * @return the company
	 */
	public String getCompany() {
		return company;
	}

	/**
	 * @param company the company to set
	 */
	public void setCompany(String company) {
		this.company = company;
	}

	/**
	 *
	 * @return
	 */
	public String getFirstname() {
		return firstname;
	}

	/**
	 *
	 * @param firstName
	 */
	public void setFirstname(String firstName) {
		this.firstname = firstName;
	}

	/**
	 *
	 * @return
	 */
	public String getLastname() {
		return lastname;
	}

	/**
	 *
	 * @param lastName
	 */
	public void setLastname(String lastName) {
		this.lastname = lastName;
	}

	/**
	 *
	 * @return
	 */
	public String getPassword() {
		return password;
	}

	/**
	 *
	 * @param password
	 */
	public void setPassword(String password) {
		this.password = password;
	}

	@Override
	public String toString() {
		return loginname + ": " + firstname + " " + lastname;
	}

	/**
	 *
	 * @return
	 */
	public FastMap getRoles() {
		return roles;
	}

	/**
	 *
	 * @param roles
	 */
	public void setRoles(FastMap roles) {
		this.roles = roles;
	}

	/**
	 *
	 * @return
	 */
	public FastMap getRights() {
		return rights;
	}

	/**
	 *
	 * @param aKey
	 * @return
	 */
	public Object getRight(String aKey) {
		return (aKey == null ? null : rights.get(aKey));
	}

	/**
	 *
	 * @param rights
	 */
	public void setRights(FastMap rights) {
		this.rights = rights;
	}

	/**
	 * Returns the user's current status (one of the ST_XXX constants) from 
	 * the internal cache. The value is NOT read from the database.
	 * @return
	 * @since 1.0
	 */
	public int getStatus() {
		return status;
	}

	/**
	 *
	 * @param status
	 */
	public void setStatus(int status) {
		this.status = status;
	}

	/**
	 * Returns the user's current password fail counter from the internal cache.
	 * The value is NOT read from the database.
	 * @return
	 * @since 1.0
	 */
	public Integer getPwdFailCount() {
		return pwdFailCount;
	}

	/**
	 * Explicitly sets the password fail counter for the user.
	 * The user is NOT automatically saved.
	 * @param aPwdFailCount
	 * @since 1.0
	 */
	public void setPwdFailCount(Integer aPwdFailCount) {
		pwdFailCount = aPwdFailCount;
	}

	/**
	 * Increments the password fail counter and saves the user back to the database.
	 * If the password fail counter exceeds the maximum value the user gets locked.
	 * This is called after the user typed an incorrect password.
	 * For this operation the SYS user is used.
	 * @return 
	 * @since 1.0
	 */
	public Integer incPwdFailCount() {
		setPwdFailCount(pwdFailCount + 1);
		if (pwdFailCount >= MAX_PWD_FAIL_COUNT) {
			lock();
		}
		return pwdFailCount;
	}

	/**
	 * Resets the password fail counter and saves the user back to the database.
	 * This is called after a successful authentication.
	 * For this operation the SYS user is used.
	 * @since 1.0
	 */
	public void resetPwdFailCount() {
		setPwdFailCount(0);
	}

	/**
	 *
	 * @return
	 */
	public String getDefaultLocale() {
		return defaultLocale;
	}

	/**
	 *
	 * @param default_locale
	 */
	public void setDefaultLocale(String default_locale) {
		this.defaultLocale = default_locale;
	}

	/**
	 *
	 * @return
	 */
	public String getCity() {
		return city;
	}

	/**
	 *
	 * @param city
	 */
	public void setCity(String city) {
		this.city = city;
	}

	/**
	 *
	 * @return
	 */
	public String getAddress() {
		return address;
	}

	/**
	 *
	 * @param address
	 */
	public void setAddress(String address) {
		this.address = address;
	}

	/**
	 *
	 * @return
	 */
	public String getZipcode() {
		return zipcode;
	}

	/**
	 *
	 * @param zipcode
	 */
	public void setZipcode(String zipcode) {
		this.zipcode = zipcode;
	}

	/**
	 *
	 * @return
	 */
	public String getCountryCode() {
		return country_code;
	}

	/**
	 *
	 * @param country_code
	 */
	public void setCountryCode(String country_code) {
		this.country_code = country_code;
	}

	/**
	 *
	 * @return
	 */
	public String getPhoneOffice() {
		return phoneOffice;
	}

	/**
	 *
	 * @param phone_office
	 */
	public void setPhoneOffice(String phone_office) {
		this.phoneOffice = phone_office;
	}

	/**
	 *
	 * @return
	 */
	public String getPhonePrivate() {
		return phonePrivate;
	}

	/**
	 *
	 * @param phone_private
	 */
	public void setPhonePrivate(String phone_private) {
		this.phonePrivate = phone_private;
	}

	/**
	 *
	 * @return
	 */
	public String getPhoneCell() {
		return phoneCell;
	}

	/**
	 *
	 * @param phone_mobile
	 */
	public void setPhoneCell(String phone_mobile) {
		this.phoneCell = phone_mobile;
	}

	/**
	 *
	 * @return
	 */
	public String getEmailOffice() {
		return emailOffice;
	}

	/**
	 *
	 * @param email_office
	 */
	public void setEmailOffice(String email_office) {
		this.emailOffice = email_office;
	}

	/**
	 *
	 * @return
	 */
	public String getEmailPrivate() {
		return emailPrivate;
	}

	/**
	 *
	 * @param email_private
	 */
	public void setEmailPrivate(String email_private) {
		this.emailPrivate = email_private;
	}

	/**
	 *
	 * @return
	 */
	public String getFaxOffice() {
		return faxOffice;
	}

	/**
	 *
	 * @param fax_office
	 */
	public void setFaxOffice(String fax_office) {
		this.faxOffice = fax_office;
	}

	/**
	 *
	 * @return
	 */
	public String getFaxPrivate() {
		return faxPrivate;
	}

	/**
	 *
	 * @param fax_private
	 */
	public void setFaxPrivate(String fax_private) {
		this.faxPrivate = fax_private;
	}

	/**
	 *
	 * @return
	 */
	public String getActivationCode() {
		return activationCode;
	}

	/**
	 *
	 * @param activation_code
	 */
	public void setActivationCode(String activation_code) {
		this.activationCode = activation_code;
	}

	/**
	 *
	 * @return
	 */
	public String getSecurityQuestion() {
		return securityQuestion;
	}

	/**
	 *
	 * @param security_question
	 */
	public void setSecurityQuestion(String security_question) {
		this.securityQuestion = security_question;
	}

	/**
	 *
	 * @return
	 */
	public String getSecurityAnswer() {
		return securityAnswer;
	}

	/**
	 *
	 * @param security_answer
	 */
	public void setSecurityAnswer(String security_answer) {
		this.securityAnswer = security_answer;
	}

	/**
	 *
	 * @return
	 */
	public int getSessionTimeout() {
		return sessionTimeout;
	}

	/**
	 *
	 * @param session_timeout
	 */
	public void setSessionTimeout(int session_timeout) {
		this.sessionTimeout = session_timeout;
	}

	/**
	 *
	 * @return
	 */
	public Properties getConfig() {
		return settings;
	}

	/**
	 *
	 * @param config
	 */
	public void setConfig(Properties config) {
		this.settings = config;
	}

	/**
	 * Sets the user to locked state and save him to the database.
	 * For this operation the SYS user is used.
	 * @since 1.0
	 */
	public void lock() {
		this.setStatus(ST_LOCKED);
	}

	/**
	 * Releases the user's locked state and saves him to the database.
	 * For this operation the SYS user is used.
	 * @since 1.0
	 */
	public void unlock() {
		this.setStatus(ST_ACTIVE);
	}

	/**
	 *
	 */
	public void initRightsAndRoles() {

	}

	/**
	 *
	 * @param aOldPW
	 * @param aNewPW
	 * @return
	 */
	public int changePassword(String aOldPW, String aNewPW) {
		if (aOldPW != null &&
			aNewPW != null &&
			password.equals(aOldPW)) {
			this.setPassword(aNewPW);
			return 1;
		} else {
			return 0;
		}
	}

	
}
