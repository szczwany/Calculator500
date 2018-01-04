package com.szczwany.calculator.Calculation;

import com.szczwany.calculator.Calculation.controller.ResultController;
import com.szczwany.calculator.Calculation.model.Calculation;
import com.szczwany.calculator.Calculation.service.CalculationService;
import com.szczwany.calculator.Helpers.CalculationFactory;
import com.szczwany.calculator.Helpers.ProjectFactory;
import com.szczwany.calculator.Project.model.Project;
import com.szczwany.calculator.Project.service.ProjectService;
import com.szczwany.calculator.Utils.Globals;
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

import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

@RunWith(SpringRunner.class)
@WebMvcTest(value = ResultController.class, secure = false)
public class ResultControllerTests
{
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProjectService projectService;

    @MockBean
    private CalculationService calculationService;

    @Test
    public void givenCalculations_whenGetCalculations_thenReturnStatusOkAndExpectSize() throws Exception
    {
        List<Calculation> calculations = CalculationFactory.createCalculations(Globals.NUM_OF_CALCULATIONS_TEST);
        given(calculationService.getCalculations()).willReturn(calculations);

        mockMvc.perform(get(Globals.ALL_CALCULATIONS_PATH)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(Globals.NUM_OF_CALCULATIONS_TEST)));
    }

    @Test
    public void givenEmptyCalculations_whenGetCalculations_thenReturnStatusNoContent() throws Exception
    {
        given(calculationService.getCalculations()).willReturn(Lists.emptyList());

        mockMvc.perform(get(Globals.ALL_CALCULATIONS_PATH)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
    }

    @Test
    public void givenCalculations_whenSetResults_thenReturnStatusOk() throws Exception
    {
        mockMvc.perform(get(Globals.ALL_CALCULATIONS_PATH + Globals.RESULT_PATH)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    public void givenCalculations_whenSetResultsByProject_thenReturnStatusOk() throws Exception
    {
        Project project = ProjectFactory.createProjectWithId();
        given(projectService.getProject(project.getId())).willReturn(project);

        mockMvc.perform(get(Globals.PROJECTS_PATH + Globals.PROJECT_ID_PATH + Globals.RESULT_PATH, project.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    public void givenCalculations_whenSetResultsByCalculation_thenReturnStatusOk() throws Exception
    {
        Project project = ProjectFactory.createProjectWithId();
        given(projectService.getProject(project.getId())).willReturn(project);
        Calculation calculation = CalculationFactory.createCalculationWithProjectAndId(project);
        given(calculationService.getCalculation(project, calculation.getId())).willReturn(calculation);

        mockMvc.perform(get(Globals.PROJECTS_PATH +
                        Globals.CALCULATIONS_PATH + Globals.CALCULATION_ID_PATH +
                        Globals.RESULT_PATH, project.getId(), calculation.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }
}