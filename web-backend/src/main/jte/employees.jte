@import java.util.List

@param List<com.statusneo.vms.model.Employee> hosts

@if(hosts != null)
    @if(hosts.isEmpty())
        <li class="px-4 py-2 text-gray-500">No employee found</li>
    @else
        @for(com.statusneo.vms.model.Employee employee : hosts)
            <li class="px-4 py-2 hover:bg-gray-100 cursor-pointer" data-id="${employee.getId()}" data-name="${employee.getName()}" onclick="selectEmployee(this.dataset.name, this.dataset.id)">${employee.getName()}</li>
        @endfor
    @endif
@endif