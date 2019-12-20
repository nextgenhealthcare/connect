/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.client.core.api.servlets;

import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;
import java.util.Properties;
import java.util.Set;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import com.mirth.connect.client.core.ClientException;
import com.mirth.connect.client.core.Operation.ExecuteType;
import com.mirth.connect.client.core.Permissions;
import com.mirth.connect.client.core.api.BaseServletInterface;
import com.mirth.connect.client.core.api.MirthOperation;
import com.mirth.connect.client.core.api.Param;
import com.mirth.connect.model.LoginStatus;
import com.mirth.connect.model.User;

@Path("/users")
@Tag(name = "Users")
@Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
public interface UserServletInterface extends BaseServletInterface {

    public static final String LOGIN_DATA_HEADER = "X-Mirth-Login-Data";

    @POST
    @Path("/_login")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Operation(summary = "Logs in to the Mirth Connect server using the specified name and password.")
    @ApiResponse(content = { @Content(mediaType = MediaType.APPLICATION_XML, examples = {
            @ExampleObject(name = "loginStatus", ref = "../apiexamples/login_status_xml") }),
            @Content(mediaType = MediaType.APPLICATION_JSON, examples = {
                    @ExampleObject(name = "loginStatus", ref = "../apiexamples/login_status_json") }) })
    @MirthOperation(name = "login", display = "Login")
    public LoginStatus login(// @formatter:off
            @Param("username") @Parameter(description = "The username to login with.", required = true, schema = @Schema(defaultValue = "admin")) @FormParam("username") String username,
            @Param(value = "password", excludeFromAudit = true) @Parameter(description = "The password to login with.", required = true, schema = @Schema(defaultValue = "admin")) @FormParam("password") String password) throws ClientException;
    // @formatter:on

    @POST
    @Path("/_logout")
    @Operation(summary = "Logs out of the server.")
    @MirthOperation(name = "logout", display = "Logout")
    public void logout() throws ClientException;

    @POST
    @Path("/")
    @Operation(summary = "Creates a new user.")
    @MirthOperation(name = "createUser", display = "Create new user", permission = Permissions.USERS_MANAGE)
    public void createUser(@Param("user") @RequestBody(description = "The User object to create.", required = true, content = {
            @Content(mediaType = MediaType.APPLICATION_XML, examples = {
                    @ExampleObject(name = "user", ref = "../apiexamples/new_user_xml") }),
            @Content(mediaType = MediaType.APPLICATION_JSON, examples = {
                    @ExampleObject(name = "user", ref = "../apiexamples/new_user_json") }) }) User user) throws ClientException;

    @GET
    @Path("/")
    @Operation(summary = "Returns a List of all users.")
    @ApiResponse(content = {
            @Content(mediaType = MediaType.APPLICATION_XML, examples = {
                    @ExampleObject(name = "allUsers", ref = "../apiexamples/user_list_xml") }),
            @Content(mediaType = MediaType.APPLICATION_JSON, examples = {
                    @ExampleObject(name = "allUsers", ref = "../apiexamples/user_list_json") }) })
    @MirthOperation(name = "getAllUsers", display = "Get all users", permission = Permissions.USERS_MANAGE, type = ExecuteType.ASYNC, auditable = false)
    public List<User> getAllUsers() throws ClientException;

    @GET
    @Path("/{userIdOrName}")
    @Operation(summary = "Returns a specific user by ID or username.")
    @ApiResponse(content = {
            @Content(mediaType = MediaType.APPLICATION_XML, examples = {
                    @ExampleObject(name = "user", ref = "../apiexamples/user_xml") }),
            @Content(mediaType = MediaType.APPLICATION_JSON, examples = {
                    @ExampleObject(name = "user", ref = "../apiexamples/user_json") }) })
    @MirthOperation(name = "getUser", display = "Get user", permission = Permissions.USERS_MANAGE, type = ExecuteType.ASYNC, auditable = false)
    public User getUser(@Param("userIdOrName") @Parameter(description = "The unique ID or username of the user to retrieve.", required = true) @PathParam("userIdOrName") String userIdOrName) throws ClientException;

    @GET
    @Path("/current")
    @Operation(summary = "Returns the current logged in user.")
    @ApiResponse(content = {
            @Content(mediaType = MediaType.APPLICATION_XML, examples = {
                    @ExampleObject(name = "user", ref = "../apiexamples/user_xml") }),
            @Content(mediaType = MediaType.APPLICATION_JSON, examples = {
                    @ExampleObject(name = "user", ref = "../apiexamples/user_json") }) })
    @MirthOperation(name = "getCurrentUser", display = "Get current user", auditable = false)
    public User getCurrentUser() throws ClientException;

    @PUT
    @Path("/{userId}")
    @Operation(summary = "Updates a specified user.")
    @MirthOperation(name = "updateUser", display = "Update user", permission = Permissions.USERS_MANAGE)
    public void updateUser(// @formatter:off
            @Param("userId") @Parameter(description = "The unique ID of the user to update.", required = true) @PathParam("userId") Integer userId,
            @Param("user") @RequestBody(description = "The User object to update.", required = true, content = {
                    @Content(mediaType = MediaType.APPLICATION_XML, examples = {
                            @ExampleObject(name = "user", ref = "../apiexamples/user_xml") }),
                    @Content(mediaType = MediaType.APPLICATION_JSON, examples = {
                            @ExampleObject(name = "user", ref = "../apiexamples/user_json") }) }) User user) throws ClientException;
    // @formatter:on

    @POST
    @Path("/_checkPassword")
    @Consumes(MediaType.TEXT_PLAIN)
    @Operation(summary = "Checks the password against the configured password policies.")
    @ApiResponse(content = { @Content(mediaType = MediaType.APPLICATION_XML, examples = {
            @ExampleObject(name = "passwordRequirementList", ref = "../apiexamples/password_requirement_list_xml") }),
            @Content(mediaType = MediaType.APPLICATION_JSON, examples = {
                    @ExampleObject(name = "passwordRequirementList", ref = "../apiexamples/password_requirement_list_json") }) })
    @MirthOperation(name = "checkUserPassword", display = "Check a password against requirements.", permission = Permissions.USERS_MANAGE)
    public List<String> checkUserPassword(@Param(value = "plainPassword", excludeFromAudit = true) @Parameter(description = "The plaintext password to check.", required = true) String plainPassword) throws ClientException;

    @PUT
    @Path("/{userId}/password")
    @Consumes(MediaType.TEXT_PLAIN)
    @Operation(summary = "Updates a user's password.")
    @ApiResponse(content = { @Content(mediaType = MediaType.APPLICATION_XML, examples = {
            @ExampleObject(name = "passwordRequirementList", ref = "../apiexamples/password_requirement_list_xml") }),
            @Content(mediaType = MediaType.APPLICATION_JSON, examples = {
                    @ExampleObject(name = "passwordRequirementList", ref = "../apiexamples/password_requirement_list_json") }) })
    @MirthOperation(name = "updateUserPassword", display = "Update a user's password", permission = Permissions.USERS_MANAGE)
    public List<String> updateUserPassword(// @formatter:off
            @Param("userId") @Parameter(description = "The unique ID of the user to update the password for.", required = true) @PathParam("userId") Integer userId,
            @Param(value = "plainPassword", excludeFromAudit = true) @Parameter(description = "The plaintext password to update with.", required = true) String plainPassword) throws ClientException;
    // @formatter:on

    @DELETE
    @Path("/{userId}")
    @Operation(summary = "Removes a specific user.")
    @MirthOperation(name = "removeUser", display = "Remove user", permission = Permissions.USERS_MANAGE)
    public void removeUser(@Param("userId") @Parameter(description = "The unique ID of the user to remove.", required = true) @PathParam("userId") Integer userId) throws ClientException;

    @GET
    @Path("/{userId}/loggedIn")
    @Operation(summary = "Returns a true if the specified user is logged in to the server.")
    @ApiResponse(content = {
            @Content(mediaType = MediaType.APPLICATION_XML, examples = {
                    @ExampleObject(name = "loggedIn", ref = "../apiexamples/boolean_xml") }),
            @Content(mediaType = MediaType.APPLICATION_JSON, examples = {
                    @ExampleObject(name = "loggedIn", ref = "../apiexamples/boolean_json") }) })
    @MirthOperation(name = "isUserLoggedIn", display = "Check if user is logged in", permission = Permissions.USERS_MANAGE)
    public boolean isUserLoggedIn(@Param("userId") @Parameter(description = "The unique ID of the user.", required = true) @PathParam("userId") Integer userId) throws ClientException;

    @GET
    @Path("/{userId}/preferences")
    @Operation(summary = "Returns a Map of user preferences, optionally filtered by a set of property names.")
    @MirthOperation(name = "getUserPreferences", display = "Get user preferences", auditable = false)
    @ApiResponse(responseCode = "200", content = {
            @Content(mediaType = MediaType.APPLICATION_XML, schema = @Schema(implementation = Properties.class), examples = {
                    @ExampleObject(name = "propertiesObject", ref = "../apiexamples/properties_xml") }),
            @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = Properties.class), examples = {
                    @ExampleObject(name = "propertiesObject", ref = "../apiexamples/properties_json") }) })
    public Properties getUserPreferences(// @formatter:off
            @Param("userId") @Parameter(description = "The unique ID of the user.", required = true) @PathParam("userId") Integer userId,
            @Param("names") @Parameter(description = "An optional set of property names to filter by.") @QueryParam("name") Set<String> names) throws ClientException;
    // @formatter:on

    @GET
    @Path("/{userId}/preferences/{name}")
    @Produces(MediaType.TEXT_PLAIN)
    @Operation(summary = "Returns a specific user preference.")
    @MirthOperation(name = "getUserPreference", display = "Get user preference", auditable = false)
    public String getUserPreference(// @formatter:off
            @Param("userId") @Parameter(description = "The unique ID of the user.", required = true) @PathParam("userId") Integer userId,
            @Param("name") @Parameter(description = "The name of the user property to retrieve.", required = true) @PathParam("name") String name) throws ClientException;
    // @formatter:on

    @PUT
    @Path("/{userId}/preferences")
    @Operation(summary = "Updates multiple user preferences.")
    @MirthOperation(name = "setUserPreferences", display = "Set user preferences")
    public void setUserPreferences(// @formatter:off
			@Param("userId") @Parameter(description = "The unique ID of the user.", required = true) @PathParam("userId") Integer userId,
			@Param("properties") @RequestBody(description = "The properties to update for the user.", required = true, content = {
					@Content(mediaType = MediaType.APPLICATION_XML, schema = @Schema(implementation = Properties.class), examples = {
							@ExampleObject(name = "properties", ref = "../apiexamples/properties_xml") }),
					@Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = Properties.class), examples = {
							@ExampleObject(name = "properties", ref = "../apiexamples/properties_json") }) }) Properties properties)
			throws ClientException;
    // @formatter:on

    @PUT
    @Path("/{userId}/preferences/{name}")
    @Consumes(MediaType.TEXT_PLAIN)
    @Operation(summary = "Updates a user preference.")
    @MirthOperation(name = "setUserPreference", display = "Set user preference")
    public void setUserPreference(// @formatter:off
            @Param("userId") @Parameter(description = "The unique ID of the user.", required = true) @PathParam("userId") Integer userId,
            @Param("name") @Parameter(description = "The name of the user property to update.", required = true) @PathParam("name") String name,
            @Param("value") @Parameter(description = "The value to update the property with.", required = true) String value) throws ClientException;
    // @formatter:on
}