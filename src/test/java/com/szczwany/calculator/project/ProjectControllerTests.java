package com.szczwany.calculator.project;

import com.szczwany.calculator.helpers.ProjectFactory;
import com.szczwany.calculator.project.controller.ProjectController;
import com.szczwany.calculator.project.exception.ProjectNotFoundException;
import com.szczwany.calculator.project.model.Project;
import com.szczwany.calculator.project.service.ProjectService;
import org.assertj.core.util.Lists;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static com.szczwany.calculator.helpers.ObjectConverter.convertToJson;
import static com.szczwany.calculator.utils.Globals.*;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.hamcrest.core.Is.is;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.mockito.internal.verification.VerificationModeFactory.times;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@WebMvcTest(value = ProjectController.class, secure = false)
public class ProjectControllerTests
{
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProjectService projectService;

    @Test
    public void givenProject_whenGetProjects_thenWillReturnStatusOkAndProjectName() throws Exception
    {
        List<Project> projects = ProjectFactory.createProjects(NUM_OF_PROJECTS_TEST);

        given(projectService.getProjects()).willReturn(projects);

        mockMvc.perform(get(PROJECTS_PATH)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name", is(projects.get(0).getName())));

        verify(projectService, times(1)).getProjects();
        verifyNoMoreInteractions(projectService);
    }

    @Test
    public void givenProjects_whenGetProjects_thenWillReturnStatusOkAndProjectsSize() throws Exception
    {
        List<Project> projects = ProjectFactory.createProjects(NUM_OF_PROJECTS_TEST);

        given(projectService.getProjects()).willReturn(projects);

        mockMvc.perform(get(PROJECTS_PATH)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(projects.size())));

        verify(projectService, times(1)).getProjects();
        verifyNoMoreInteractions(projectService);
    }

    @Test
    public void givenEmptyProjects_whenGetProjects_thenWillReturnStatusNoContent() throws Exception
    {
        given(projectService.getProjects()).willReturn(Lists.emptyList());

        mockMvc.perform(get(PROJECTS_PATH)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        verify(projectService, times(1)).getProjects();
        verifyNoMoreInteractions(projectService);
    }

    @Test
    public void givenProject_whenAddProject_thenExpectStatusCreated() throws Exception
    {
        Project project = ProjectFactory.createProjectWithId();

        doNothing().when(projectService).addProject(project);

        mockMvc.perform(post(PROJECTS_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .content(convertToJson(project)))
                .andExpect(status().isCreated());
    }

    @Test
    public void givenEmptyProject_whenAddProject_thenExpectStatusBadRequest() throws Exception
    {
        Project project = ProjectFactory.createEmptyProject();

        doNothing().when(projectService).addProject(project);

        mockMvc.perform(post(PROJECTS_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .content(convertToJson(project)))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void givenProjectId_whenGetProject_thenWillReturnProject() throws Exception
    {
        Project project = ProjectFactory.createProjectWithId();

        given(projectService.getProject(project.getId())).willReturn(project);

        mockMvc.perform(get(PROJECTS_PATH + PROJECT_ID_PATH, project.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(project.getId().intValue())))
                .andExpect(jsonPath("$.name", is(project.getName())))
                .andExpect(jsonPath("$.calculations", is(project.getCalculations())));
    }

    @Test
    public void givenProjectId_whenGetProject_thenWillReturnStatusNotFoundAndErrorMessage() throws Exception
    {
        Project project = ProjectFactory.createProjectWithId();

        when(projectService.getProject(project.getId())).thenThrow(new ProjectNotFoundException(project.getId()));

        mockMvc.perform(get(PROJECTS_PATH + PROJECT_ID_PATH, project.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorMessage", is("project '" + project.getId() + "' does not exist")));
    }

    @Test
    public void givenProject_whenUpdateProject_thenWillReturnStatusNoContent() throws Exception
    {
        Project project = ProjectFactory.createProjectWithId();

        when(projectService.getProject(project.getId())).thenReturn(project);
        doNothing().when(projectService).updateProject(project);

        mockMvc.perform(put(PROJECTS_PATH + PROJECT_ID_PATH, project.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(convertToJson(project)))
                .andExpect(status().isNoContent());
    }

    @Test
    public void givenProjectIdAndNullResponseBody_whenUpdateProject_thenWillReturnStatusBadRequest() throws Exception
    {
        Project project = ProjectFactory.createProjectWithId();

        when(projectService.getProject(project.getId())).thenReturn(project);
        doNothing().when(projectService).updateProject(project);

        mockMvc.perform(put(PROJECTS_PATH + PROJECT_ID_PATH, project.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(convertToJson(null)))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void givenProjectId_whenUpdateProject_thenWillReturnStatusNotFound() throws Exception
    {
        Project project = ProjectFactory.createProjectWithId();

        when(projectService.getProject(project.getId())).thenThrow(new ProjectNotFoundException(project.getId()));

        mockMvc.perform(put(PROJECTS_PATH + PROJECT_ID_PATH, project.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(convertToJson(project)))
                .andExpect(status().isNotFound());
    }

    @Test
    public void givenProjectId_whenDeleteProject_thenWillReturnStatusNoContent() throws Exception
    {
        Project project = ProjectFactory.createProjectWithId();

        when(projectService.getProject(project.getId())).thenReturn(project);
        doNothing().when(projectService).deleteProject(project.getId());

        mockMvc.perform(delete(PROJECTS_PATH + PROJECT_ID_PATH, project.getId()))
                .andExpect(status().isNoContent());
    }

    @Test
    public void givenProjectId_whenDeleteProject_thenWillReturnStatusNotFound() throws Exception
    {
        Project project = ProjectFactory.createProjectWithId();

        when(projectService.getProject(project.getId())).thenThrow(new ProjectNotFoundException(project.getId()));

        mockMvc.perform(delete(PROJECTS_PATH + PROJECT_ID_PATH, project.getId()))
                .andExpect(status().isNotFound());
    }
}

