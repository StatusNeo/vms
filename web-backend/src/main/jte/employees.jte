@import java.util.List

@param List<String> employees

@if(employees != null)
    <div id="employeeSuggestions" class="absolute bg-white shadow-md w-full mt-1 rounded-md z-10">
        @if(employees.isEmpty())
            <div class="px-4 py-2 text-gray-500">No employees found</div>
        @else
            <ul>
                @for(String employee : employees)
                    <li class="px-4 py-2 hover:bg-gray-100 cursor-pointer">${employee}</li>
                @endfor
            </ul>
        @endif
    </div>
@endif