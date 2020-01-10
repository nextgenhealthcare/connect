/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.client.core.api.servlets;

import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.glassfish.jersey.media.multipart.FormDataParam;

import com.mirth.connect.client.core.ClientException;
import com.mirth.connect.client.core.Operation.ExecuteType;
import com.mirth.connect.client.core.Permissions;
import com.mirth.connect.client.core.api.BaseServletInterface;
import com.mirth.connect.client.core.api.MirthOperation;
import com.mirth.connect.client.core.api.Param;
import com.mirth.connect.model.codetemplates.CodeTemplate;
import com.mirth.connect.model.codetemplates.CodeTemplateLibrary;
import com.mirth.connect.model.codetemplates.CodeTemplateLibrarySaveResult;
import com.mirth.connect.model.codetemplates.CodeTemplateSummary;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

@Path("/")
@Tag(name = "Code Templates")
@Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
public interface CodeTemplateServletInterface extends BaseServletInterface {

    @GET
    @Path("/codeTemplateLibraries")
    @Operation(summary = "Retrieves multiple code template libraries by ID, or all libraries if not specified.")
    @ApiResponse(content = { @Content(mediaType = MediaType.APPLICATION_XML, examples = {
            @ExampleObject(name = "code_template_library_list", ref = "../apiexamples/code_template_library_list_xml"),
            @ExampleObject(name = "code_template_library_list_full_templates", ref = "../apiexamples/code_template_library_list_full_templates_xml") }),
            @Content(mediaType = MediaType.APPLICATION_JSON, examples = {
                    @ExampleObject(name = "code_template_library_list", ref = "../apiexamples/code_template_library_list_json"),
                    @ExampleObject(name = "code_template_library_list_full_templates", ref = "../apiexamples/code_template_library_list_full_templates_json") }) })
    @MirthOperation(name = "getCodeTemplateLibraries", display = "Get code template libraries", permission = Permissions.CODE_TEMPLATES_VIEW, type = ExecuteType.ASYNC, auditable = false)
    public List<CodeTemplateLibrary> getCodeTemplateLibraries(// @formatter:off
            @Param("libraryIds") @Parameter(description = "The ID of the library(s) to retrieve.") @QueryParam("libraryId") Set<String> libraryIds,
            @Param("includeCodeTemplates") @Parameter(description = "If true, full code templates will be included inside each library.", schema = @Schema(defaultValue = "false")) @QueryParam("includeCodeTemplates") boolean includeCodeTemplates) throws ClientException;
    // @formatter:on

    @POST
    @Path("/codeTemplateLibraries/_getCodeTemplateLibraries")
    @Operation(summary = "Retrieves multiple code template libraries by ID, or all libraries if not specified. This is a POST request alternative to GET /codeTemplateLibraries that may be used when there are too many library IDs to include in the query parameters.")
    @ApiResponse(content = { @Content(mediaType = MediaType.APPLICATION_XML, examples = {
            @ExampleObject(name = "code_template_library_list", ref = "../apiexamples/code_template_library_list_xml"),
            @ExampleObject(name = "code_template_library_list_full_templates", ref = "../apiexamples/code_template_library_list_full_templates_xml") }),
            @Content(mediaType = MediaType.APPLICATION_JSON, examples = {
                    @ExampleObject(name = "code_template_library_list", ref = "../apiexamples/code_template_library_list_json"),
                    @ExampleObject(name = "code_template_library_list_full_templates", ref = "../apiexamples/code_template_library_list_full_templates_json") }) })
    @MirthOperation(name = "getCodeTemplateLibraries", display = "Get code template libraries", permission = Permissions.CODE_TEMPLATES_VIEW, type = ExecuteType.ASYNC, auditable = false)
    public List<CodeTemplateLibrary> getCodeTemplateLibrariesPost(// @formatter:off
            @Param("libraryIds") 
            @RequestBody(description = "The ID of the library(s) to retrieve.", content = {
                    @Content(mediaType = MediaType.APPLICATION_XML, examples = {
                            @ExampleObject(name = "code_template_library_id_set", ref = "../apiexamples/guid_set_xml") }),
                    @Content(mediaType = MediaType.APPLICATION_JSON, examples = {
                            @ExampleObject(name = "code_template_library_id_set", ref = "../apiexamples/guid_set_json") }) })
            Set<String> libraryIds,
            @Param("includeCodeTemplates") @Parameter(description = "If true, full code templates will be included inside each library.", schema = @Schema(defaultValue = "false")) @QueryParam("includeCodeTemplates") boolean includeCodeTemplates) throws ClientException;
    // @formatter:on

    @GET
    @Path("/codeTemplateLibraries/{libraryId}")
    @Operation(summary = "Retrieves a single code template library.")
    @ApiResponse(content = { @Content(mediaType = MediaType.APPLICATION_XML, examples = {
            @ExampleObject(name = "code_template_library", ref = "../apiexamples/code_template_library_xml"),
            @ExampleObject(name = "code_template_library_full_templates", ref = "../apiexamples/code_template_library_full_templates_xml") }),
            @Content(mediaType = MediaType.APPLICATION_JSON, examples = {
                    @ExampleObject(name = "code_template_library", ref = "../apiexamples/code_template_library_json"),
                    @ExampleObject(name = "code_template_library_full_templates", ref = "../apiexamples/code_template_library_full_templates_json") }) })
    @MirthOperation(name = "getCodeTemplateLibrary", display = "Get code template library", permission = Permissions.CODE_TEMPLATES_VIEW, type = ExecuteType.ASYNC)
    public CodeTemplateLibrary getCodeTemplateLibrary(// @formatter:off
            @Param("libraryId") @Parameter(description = "The ID of the library to retrieve.") @PathParam("libraryId") String libraryId,
            @Param("includeCodeTemplates") @Parameter(description = "If true, full code templates will be included inside each library.", schema = @Schema(defaultValue = "false")) @QueryParam("includeCodeTemplates") boolean includeCodeTemplates) throws ClientException;
    // @formatter:on

    @PUT
    @Path("/codeTemplateLibraries")
    @Operation(summary = "Replaces all code template libraries.")
    @ApiResponse(content = { @Content(mediaType = MediaType.APPLICATION_XML, examples = {
            @ExampleObject(name = "libraries_replaced", ref = "../apiexamples/boolean_xml") }),
            @Content(mediaType = MediaType.APPLICATION_JSON, examples = {
                    @ExampleObject(name = "libraries_replaced", ref = "../apiexamples/boolean_json") }) })
    @MirthOperation(name = "updateCodeTemplateLibraries", display = "Update code template libraries", permission = Permissions.CODE_TEMPLATES_MANAGE)
    public boolean updateCodeTemplateLibraries(// @formatter:off
            @Param("libraries") 
            @RequestBody(description = "The list of code template libraries to replace with.", required = true, content = {
                    @Content(mediaType = MediaType.APPLICATION_XML, examples = {
                            @ExampleObject(name = "code_template_library_list_full_templates", ref = "../apiexamples/code_template_library_list_full_templates_xml") }),
                    @Content(mediaType = MediaType.APPLICATION_JSON, examples = {
                            @ExampleObject(name = "code_template_library_list_full_templates", ref = "../apiexamples/code_template_library_list_full_templates_json") }) })
            List<CodeTemplateLibrary> libraries,
            @Param("override") @Parameter(description = "If true, the code template library will be updated even if a different revision exists on the server.", schema = @Schema(defaultValue = "false")) @QueryParam("override") boolean override) throws ClientException;
    // @formatter:on

    @GET
    @Path("/codeTemplates")
    @Operation(summary = "Retrieves multiple code templates by ID, or all templates if not specified.")
    @ApiResponse(content = { @Content(mediaType = MediaType.APPLICATION_XML, examples = {
            @ExampleObject(name = "code_template_list", ref = "../apiexamples/code_template_list_xml") }),
            @Content(mediaType = MediaType.APPLICATION_JSON, examples = {
                    @ExampleObject(name = "code_template_list", ref = "../apiexamples/code_template_list_json") }) })
    @MirthOperation(name = "getCodeTemplates", display = "Get code templates", permission = Permissions.CODE_TEMPLATES_VIEW)
    public List<CodeTemplate> getCodeTemplates(@Param("codeTemplateIds") @Parameter(description = "The ID of the code template(s) to retrieve.") @QueryParam("codeTemplateId") Set<String> codeTemplateIds) throws ClientException;

    @POST
    @Path("/codeTemplates/_getCodeTemplates")
    @Operation(summary = "Retrieves multiple code templates by ID, or all templates if not specified. This is a POST request alternative to GET /codeTemplates that may be used when there are too many code template IDs to include in the query parameters.")
    @ApiResponse(content = { @Content(mediaType = MediaType.APPLICATION_XML, examples = {
            @ExampleObject(name = "code_template_list", ref = "../apiexamples/code_template_list_xml") }),
            @Content(mediaType = MediaType.APPLICATION_JSON, examples = {
                    @ExampleObject(name = "code_template_list", ref = "../apiexamples/code_template_list_json") }) })
    @MirthOperation(name = "getCodeTemplates", display = "Get code templates", permission = Permissions.CODE_TEMPLATES_VIEW)
    public List<CodeTemplate> getCodeTemplatesPost(@Param("codeTemplateIds") @RequestBody(description = "The ID of the code template(s) to retrieve.", content = {
            @Content(mediaType = MediaType.APPLICATION_XML, examples = {
                    @ExampleObject(name = "code_template_id_set", ref = "../apiexamples/guid_set_xml") }),
            @Content(mediaType = MediaType.APPLICATION_JSON, examples = {
                    @ExampleObject(name = "code_template_id_set", ref = "../apiexamples/guid_set_json") }) }) Set<String> codeTemplateIds) throws ClientException;

    @GET
    @Path("/codeTemplates/{codeTemplateId}")
    @Operation(summary = "Retrieves a single code template.")
    @ApiResponse(content = { @Content(mediaType = MediaType.APPLICATION_XML, examples = {
            @ExampleObject(name = "code_template", ref = "../apiexamples/code_template_xml") }),
            @Content(mediaType = MediaType.APPLICATION_JSON, examples = {
                    @ExampleObject(name = "code_template", ref = "../apiexamples/code_template_json") }) })
    @MirthOperation(name = "getCodeTemplates", display = "Get code templates", permission = Permissions.CODE_TEMPLATES_VIEW)
    public CodeTemplate getCodeTemplate(@Param("codeTemplateId") @Parameter(description = "The ID of the code template to retrieve.") @PathParam("codeTemplateId") String codeTemplateId) throws ClientException;

    @POST
    @Path("/codeTemplates/_getSummary")
    @Operation(summary = "Returns a list of code template summaries, indicating to a client which code templates have changed. If a code template was modified, the entire CodeTemplate object will be returned.")
    @ApiResponse(content = { @Content(mediaType = MediaType.APPLICATION_XML, examples = {
            @ExampleObject(name = "code_template_summary_list", ref = "../apiexamples/code_template_summary_list_xml"),
            @ExampleObject(name = "code_template_summary_list_revision_changed", ref = "../apiexamples/code_template_summary_list_revision_changed_xml") }),
            @Content(mediaType = MediaType.APPLICATION_JSON, examples = {
                    @ExampleObject(name = "code_template_summary_list", ref = "../apiexamples/code_template_summary_list_json"),
                    @ExampleObject(name = "code_template_summary_list_revision_changed", ref = "../apiexamples/code_template_summary_list_revision_changed_json") }) })
    @MirthOperation(name = "getCodeTemplateSummary", display = "Get code template summary", permission = Permissions.CODE_TEMPLATES_VIEW, type = ExecuteType.ASYNC, auditable = false)
    public List<CodeTemplateSummary> getCodeTemplateSummary(@Param("clientRevisions") @RequestBody(description = "A map of revisions telling the server the state of the client-side code template cache.", required = true, content = {
            @Content(mediaType = MediaType.APPLICATION_XML, examples = {
                    @ExampleObject(name = "guid_to_int_map", ref = "../apiexamples/guid_to_int_map_xml") }),
            @Content(mediaType = MediaType.APPLICATION_JSON, examples = {
                    @ExampleObject(name = "guid_to_int_map", ref = "../apiexamples/guid_to_int_map_json") }) }) Map<String, Integer> clientRevisions) throws ClientException;

    @PUT
    @Path("/codeTemplates/{codeTemplateId}")
    @Operation(summary = "Updates a single code template.")
    @ApiResponse(content = { @Content(mediaType = MediaType.APPLICATION_XML, examples = {
            @ExampleObject(name = "code_template_replaced", ref = "../apiexamples/boolean_xml") }),
            @Content(mediaType = MediaType.APPLICATION_JSON, examples = {
                    @ExampleObject(name = "code_template_replaced", ref = "../apiexamples/boolean_json") }) })
    @MirthOperation(name = "updateCodeTemplate", display = "Update code template", permission = Permissions.CODE_TEMPLATES_MANAGE)
    public boolean updateCodeTemplate(// @formatter:off
            @Param("codeTemplateId") @Parameter(description = "The ID of the code template.", required = true) @PathParam("codeTemplateId") String codeTemplateId,
            
            @Param("codeTemplate") 
            @RequestBody(description = "The CodeTemplate object to update with.", required = true, content = {
                    @Content(mediaType = MediaType.APPLICATION_XML, examples = {
                            @ExampleObject(name = "code_template", ref = "../apiexamples/code_template_xml") }),
                    @Content(mediaType = MediaType.APPLICATION_JSON, examples = {
                            @ExampleObject(name = "code_template", ref = "../apiexamples/code_template_json") }) })
            CodeTemplate codeTemplate,
            
            @Param("override") @Parameter(description = "If true, the code template will be updated even if a different revision exists on the server.", schema = @Schema(defaultValue = "false")) @QueryParam("override") boolean override) throws ClientException;
    // @formatter:on

    @DELETE
    @Path("/codeTemplates/{codeTemplateId}")
    @Operation(summary = "Removes a single code template.")
    @MirthOperation(name = "removeCodeTemplate", display = "Remove code template", permission = Permissions.CODE_TEMPLATES_MANAGE)
    public void removeCodeTemplate(@Param("codeTemplateId") @Parameter(description = "The ID of the code template.", required = true) @PathParam("codeTemplateId") String codeTemplateId) throws ClientException;

    @POST
    @Path("/codeTemplateLibraries/_bulkUpdate")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Operation(summary = "Updates all libraries and updates/removes selected code templates in one request. " + SWAGGER_TRY_IT_OUT_DISCLAIMER)
    @ApiResponse(content = { @Content(mediaType = MediaType.APPLICATION_XML, examples = {
            @ExampleObject(name = "code_template_library_saved_result", ref = "../apiexamples/code_template_library_saved_result_xml") }),
            @Content(mediaType = MediaType.APPLICATION_JSON, examples = {
                    @ExampleObject(name = "code_template_library_saved_result", ref = "../apiexamples/code_template_library_saved_result_json") }) })
    @MirthOperation(name = "updateCodeTemplatesAndLibraries", display = "Update code templates and libraries", permission = Permissions.CODE_TEMPLATES_MANAGE)
    public CodeTemplateLibrarySaveResult updateLibrariesAndTemplates(// @formatter:off
            @Param("libraries") 
            @Parameter(description = "The set of code template libraries to replace with.", schema = @Schema(description = "The set of code template libraries to replace with.")) 
            @FormDataParam("libraries") 
            List<CodeTemplateLibrary> libraries,
            
            @Param("removedLibraryIds") 
            @Parameter(description = "All library IDs known to be removed.", schema = @Schema(description = "All library IDs known to be removed.")) 
            @FormDataParam("removedLibraryIds") 
            Set<String> removedLibraryIds,
            
            @Param("updatedCodeTemplates") 
            @Parameter(description = "The set of code templates to update.", schema = @Schema(description = "The set of code templates to update.")) 
            @FormDataParam("updatedCodeTemplates") 
            List<CodeTemplate> updatedCodeTemplates,
            
            @Param("removedCodeTemplateIds") 
            @Parameter(description = "All code template IDs known to be removed.", schema = @Schema(description = "All code template IDs known to be removed.")) 
            @FormDataParam("removedCodeTemplateIds") 
            Set<String> removedCodeTemplateIds,
            
            @Param("override") 
            @Parameter(description = "If true, the libraries and code templates will be updated even if different revisions exist on the server.", schema = @Schema(defaultValue = "false")) 
            @QueryParam("override") 
            boolean override) throws ClientException;
    // @formatter:on
}