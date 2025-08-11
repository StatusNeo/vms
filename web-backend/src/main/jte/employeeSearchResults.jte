@import java.util.List
@param List<String> employees

@if (employees.isEmpty())
    <li class="px-3 py-2 text-gray-400">No employees found.</li>
@else
    @for (String name : employees)
        <li class="px-3 py-2 hover:bg-gray-100 cursor-pointer"
            onclick="selectEmployee('@{name}')">
            ${name}
        </li>
    @endfor
@endif
