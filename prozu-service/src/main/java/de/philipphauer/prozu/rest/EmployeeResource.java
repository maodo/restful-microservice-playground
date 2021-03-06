package de.philipphauer.prozu.rest;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Optional;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import com.google.inject.Inject;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;
import com.wordnik.swagger.annotations.ApiResponse;
import com.wordnik.swagger.annotations.ApiResponses;

import de.philipphauer.prozu.model.Employee;
import de.philipphauer.prozu.model.ProjectDays;
import de.philipphauer.prozu.repo.EmployeeDAO;
import de.philipphauer.prozu.rest.exception.ProZuClientErrorException;
import de.philipphauer.prozu.rest.request.EmployeeData;
import de.philipphauer.prozu.rest.responses.EmployeeResponse;
import de.philipphauer.prozu.rest.responses.EmployeesResponse;
import de.philipphauer.prozu.rest.responses.ProjectDaysResponse;
import de.philipphauer.prozu.rest.util.EntityMapper;
import de.philipphauer.prozu.rest.util.MediaTypeWithCharset;
import de.philipphauer.prozu.util.ser.DummyDataInitializer;

@Api(value = URLConstants.EMPLOYEES, description = "REST API to interact with employee resources.")
@Path(URLConstants.EMPLOYEES)
public class EmployeeResource {

	@Context
	private UriInfo context;
	@Context
	private HttpHeaders headers;
	@Inject
	private EmployeeDAO dao;
	@Inject
	private EntityMapper mapper;
	@Inject
	private DummyDataInitializer dummyInitializer;

	// TODO better http status code on failures, illegal inputs, exceptions

	/*
	 * GET, Read
	 */

	@ApiOperation(value = "Get all employees", notes = "Interact with employee resources", response = EmployeesResponse.class)
	@GET
	@Path("/")
	@Produces(MediaTypeWithCharset.APPLICATION_JSON_UTF8)
	public EmployeesResponse getAllEmployees(
			@ApiParam(value = "limits the result set", required = false, defaultValue = "10") @QueryParam("limit") Integer limit,
			@ApiParam(value = "the offset", required = false, defaultValue = "0") @QueryParam("offset") Integer offset,
			@ApiParam(value = "search for names that contains the given string. not case sensetive. ", required = false) @QueryParam("search") String search) {
		initDummyDataIfDatabaseEmpty();
		int usedLimit = limit == null ? 10 : limit;
		int usedOffset = offset == null ? 0 : offset;
		Optional<String> usedSearch = Optional.ofNullable(search);
		List<Employee> employees = dao.getEmployees(usedLimit, usedOffset, usedSearch);
		long totalCount = dao.getEmployeeCount(usedSearch);
		EmployeesResponse response = mapper.mapToEmployeesResponse(employees, usedLimit, usedOffset, totalCount,
				usedSearch);
		return response;
	}

	private void initDummyDataIfDatabaseEmpty() {
		long employeeCount = dao.getEmployeeCount(Optional.empty());
		if (employeeCount == 0){
			dummyInitializer.initDummyData();
		}
	}

	@ApiOperation(value = "Get an employees wit a certain ID", response = EmployeeResponse.class)
	@ApiResponses(value = {
			@ApiResponse(code = 400, message = "Invalid ID supplied"),
	})
	@GET
	@Path("/{employeeId}")
	@Produces(MediaTypeWithCharset.APPLICATION_JSON_UTF8)
	public EmployeeResponse getEmployee(
			@ApiParam(value = "the id of the employee", required = true) @PathParam("employeeId") String employeeId) {
		Optional<Employee> employee = dao.getEmployee(employeeId);
		if (employee.isPresent()) {
			EmployeeResponse rEmployee = mapper.mapToREmployee(employee.get());
			return rEmployee;
		}
		throw new ProZuClientErrorException("No Employee with id " + employeeId + " found.", 9);
	}

	@ApiOperation(value = "Get the project day for an employee", response = ProjectDaysResponse.class)
	@GET
	@Path("/{employeeId}/projectdays/")
	@Produces(MediaTypeWithCharset.APPLICATION_JSON_UTF8)
	public List<ProjectDaysResponse> getAllProjectDays(
			@PathParam("employeeId") String employeeId) {
		List<ProjectDays> projectDays = dao.getAllProjectDays(employeeId);
		List<ProjectDaysResponse> rProjectDays = mapper.mapToRProjectDays(projectDays);
		return rProjectDays;
	}

	/*
	 * POST, Create
	 */

	@ApiOperation(value = "Creates a new employee")
	@POST
	@Path("/")
	@Consumes(MediaTypeWithCharset.APPLICATION_JSON_UTF8)
	public Response createEmployee(
			@ApiParam(value = "employee data", required = true) EmployeeData newEmployeeData)
			throws URISyntaxException {
		Employee employee = dao.createEmployee(newEmployeeData.getName());

		URI uri = createNewLocationURI(employee.getId());
		Response response = Response.created(uri).build();

		return response;
	}

	private URI createNewLocationURI(String employeeId) throws URISyntaxException {
		String uriString = context.getAbsolutePath().toString();
		if (!uriString.endsWith("/")) {
			uriString += "/";
		}
		return new URI(uriString + employeeId);
	}

	/*
	 * PUT, Update
	 */

	@ApiOperation(value = "Updates a given employee")
	@PUT
	@Path("/{employeeId}")
	@Consumes(MediaTypeWithCharset.APPLICATION_JSON_UTF8)
	public Response updateEmployee(
			@ApiParam(value = "the id of the employee to be updated") @PathParam("employeeId") String employeeId,
			@ApiParam(value = "the new employee data", required = true) EmployeeData newEmployeeData) {
		String name = newEmployeeData.getName();
		dao.updateEmployee(employeeId, name);
		return Response.ok().build();
	}

	/*
	 * DELETE, delete
	 */

	@ApiOperation(value = "Deletes an employee")
	@DELETE
	@Path("/{employeeId}")
	public Response deleteEmployee(@PathParam("employeeId") String employeeId) {
		dao.deleteEmployee(employeeId);
		return Response.ok().build();
	}

}
