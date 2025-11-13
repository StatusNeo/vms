@import java.util.List

@param List<com.statusneo.vms.model.Employee> employees

@if(employees != null)
    @if(employees.isEmpty())
        <li class="px-4 py-2 text-gray-500">No employees found</li>
    @else
        @for(com.statusneo.vms.model.Employee employee : employees)
            <li class="px-4 py-2 hover:bg-gray-100 cursor-pointer" data-id="${employee.getId()}" data-name="${employee.getName()}" onclick="selectEmployee(this.dataset.name, this.dataset.id)">${employee.getName()}</li>
        @endfor
    @endif
@endif