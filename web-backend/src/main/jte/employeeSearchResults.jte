@import com.statusneo.vms.model.Employee
@param java.util.List<Employee> employees

@if (employees.isEmpty())
    <li class="px-3 py-2 text-gray-400">No employees found.</li>
@else
    @for (Employee employee : employees)
        <li class="px-3 py-2 hover:bg-gray-100 cursor-pointer"
            onclick="selectEmployee('@{employee.getName()}')">
            ${employee.getName()}
        </li>
    @endfor
@endif
