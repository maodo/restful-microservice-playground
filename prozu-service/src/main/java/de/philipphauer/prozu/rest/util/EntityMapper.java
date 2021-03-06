package de.philipphauer.prozu.rest.util;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.ws.rs.core.UriInfo;

import de.philipphauer.prozu.model.Employee;
import de.philipphauer.prozu.model.ProjectDays;
import de.philipphauer.prozu.rest.responses.EmployeeResponse;
import de.philipphauer.prozu.rest.responses.EmployeesResponse;
import de.philipphauer.prozu.rest.responses.ProjectDaysResponse;

public class EntityMapper {

	@Inject
	private UriInfo context;

	public EmployeesResponse mapToEmployeesResponse(List<Employee> employees, int limit, int offset,
			long totalCount, Optional<String> search) {
		List<EmployeeResponse> employeeResponses = employees.stream()
				.map((employee) -> mapToREmployee(employee))
				.collect(Collectors.toList());
		return new EmployeesResponse(limit, offset, totalCount, employeeResponses, search, context.getBaseUri());
	}

	public EmployeeResponse mapToREmployee(Employee input) {
		return new EmployeeResponse(input.getId(), input.getName(), context.getBaseUri());
	}

	public List<ProjectDaysResponse> mapToRProjectDays(List<ProjectDays> projectDays) {
		return projectDays.stream()
				.map((projectDay) -> mapToRProjectDay(projectDay))
				.collect(Collectors.toList());
	}

	public ProjectDaysResponse mapToRProjectDay(ProjectDays input) {
		return new ProjectDaysResponse(input.getMonth(), input.getDaysCount());
	}
}
