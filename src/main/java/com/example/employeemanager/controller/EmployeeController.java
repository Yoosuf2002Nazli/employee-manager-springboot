package com.example.employeemanager.controller;

import com.example.employeemanager.model.Employee;
import com.example.employeemanager.service.EmployeeService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.http.ResponseEntity;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class EmployeeController {

    private final EmployeeService employeeService;

    public EmployeeController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    @GetMapping(value = {"/", "/employees"}, produces = MediaType.TEXT_HTML_VALUE)
    public String listEmployees(Model model) {
        if (!model.containsAttribute("employee")) {
            model.addAttribute("employee", new Employee());
        }
        model.addAttribute("employees", employeeService.findAll());
        return "employees";
    }

    @GetMapping(value = "/employees", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public List<Employee> getEmployeesAsJsonForPostman() {
        return employeeService.findAll();
    }

    @GetMapping("/api/employees")
    @ResponseBody
    public List<Employee> getEmployeesAsJson() {
        return employeeService.findAll();
    }

    @GetMapping("/api/employees/{id}")
    @ResponseBody
    public ResponseEntity<Employee> getEmployeeAsJson(@PathVariable Long id) {
        return employeeService.findById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping("/employees")
    public String saveEmployee(@Valid @ModelAttribute("employee") Employee employee,
                               BindingResult bindingResult,
                               RedirectAttributes redirectAttributes,
                               Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("employees", employeeService.findAll());
            return "employees";
        }
        if (employeeService.emailExists(employee.getEmail())) {
            bindingResult.rejectValue("email", "duplicate", "This email is already registered");
            model.addAttribute("employees", employeeService.findAll());
            return "employees";
        }
        employeeService.save(employee);
        redirectAttributes.addFlashAttribute("success", "Employee added successfully.");
        return "redirect:/employees";
    }

    @GetMapping("/employees/{id}/edit")
    public String showEditForm(@PathVariable Long id, Model model,
                               RedirectAttributes redirectAttributes) {
        return employeeService.findById(id)
                .map(employee -> {
                    model.addAttribute("employee", employee);
                    return "edit-employee";
                })
                .orElseGet(() -> {
                    redirectAttributes.addFlashAttribute("error", "Employee not found.");
                    return "redirect:/employees";
                });
    }

    @PostMapping("/employees/{id}/update")
    public String updateEmployee(@PathVariable Long id,
                                 @Valid @ModelAttribute("employee") Employee employee,
                                 BindingResult bindingResult,
                                 RedirectAttributes redirectAttributes,
                                 Model model) {
        employee.setId(id);
        if (bindingResult.hasErrors()) {
            return "edit-employee";
        }
        if (employeeService.findById(id).isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Employee not found.");
            return "redirect:/employees";
        }
        if (employeeService.emailExistsForAnotherEmployee(employee.getEmail(), id)) {
            bindingResult.rejectValue("email", "duplicate", "This email is already registered");
            return "edit-employee";
        }
        employeeService.save(employee);
        redirectAttributes.addFlashAttribute("success", "Employee updated successfully.");
        return "redirect:/employees";
    }

    @PostMapping("/employees/{id}/delete")
    public String deleteEmployee(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        if (employeeService.findById(id).isPresent()) {
            employeeService.deleteById(id);
            redirectAttributes.addFlashAttribute("success", "Employee deleted successfully.");
        } else {
            redirectAttributes.addFlashAttribute("error", "Employee not found.");
        }
        return "redirect:/employees";
    }
}
