package com.duy.ide.features.templates

import javax.inject.Inject

class LiveTemplateManager @Inject constructor() {
    private val templates = mutableMapOf<String, LiveTemplate>()
    private val categories = mutableMapOf<String, List<String>>()

    fun registerTemplate(template: LiveTemplate) {
        templates[template.id] = template
        updateCategory(template.category, template.id)
    }

    fun getTemplate(id: String): LiveTemplate? = templates[id]

    fun getTemplatesByCategory(category: String): List<LiveTemplate> {
        return categories[category]?.mapNotNull { templates[it] } ?: emptyList()
    }

    fun expandTemplate(template: LiveTemplate, variables: Map<String, String>): String {
        var result = template.content
        variables.forEach { (key, value) ->
            result = result.replace("\${$key}", value)
        }
        return result
    }

    private fun updateCategory(category: String, templateId: String) {
        categories[category] = (categories[category] ?: emptyList()) + templateId
    }

    // Templates predefinidos
    fun loadDefaultTemplates() {
        // Java Templates
        registerTemplate(LiveTemplate(
            id = "junit-test",
            name = "JUnit Test Method",
            description = "Creates a JUnit test method",
            category = "Java Tests",
            content = """
                @Test
                public void test\${NAME}() {
                    \${BODY}
                }
            """.trimIndent()
        ))

        registerTemplate(LiveTemplate(
            id = "singleton",
            name = "Singleton Pattern",
            description = "Creates a singleton class",
            category = "Design Patterns",
            content = """
                public class \${CLASS_NAME} {
                    private static \${CLASS_NAME} instance;
                    
                    private \${CLASS_NAME}() {}
                    
                    public static \${CLASS_NAME} getInstance() {
                        if (instance == null) {
                            instance = new \${CLASS_NAME}();
                        }
                        return instance;
                    }
                }
            """.trimIndent()
        ))

        // Android Templates
        registerTemplate(LiveTemplate(
            id = "recycler-adapter",
            name = "RecyclerView Adapter",
            description = "Creates a RecyclerView adapter",
            category = "Android",
            content = """
                public class \${ADAPTER_NAME} extends RecyclerView.Adapter<\${ADAPTER_NAME}.ViewHolder> {
                    private List<\${MODEL_NAME}> items;
                    
                    public \${ADAPTER_NAME}(List<\${MODEL_NAME}> items) {
                        this.items = items;
                    }
                    
                    @Override
                    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                        View view = LayoutInflater.from(parent.getContext())
                            .inflate(R.layout.\${LAYOUT_NAME}, parent, false);
                        return new ViewHolder(view);
                    }
                    
                    @Override
                    public void onBindViewHolder(ViewHolder holder, int position) {
                        \${MODEL_NAME} item = items.get(position);
                        \${BIND_CODE}
                    }
                    
                    @Override
                    public int getItemCount() {
                        return items.size();
                    }
                    
                    static class ViewHolder extends RecyclerView.ViewHolder {
                        \${VIEW_HOLDER_FIELDS}
                        
                        ViewHolder(View view) {
                            super(view);
                            \${VIEW_HOLDER_INIT}
                        }
                    }
                }
            """.trimIndent()
        ))
    }
}

data class LiveTemplate(
    val id: String,
    val name: String,
    val description: String,
    val category: String,
    val content: String,
    val variables: List<TemplateVariable> = emptyList()
)

data class TemplateVariable(
    val name: String,
    val defaultValue: String = "",
    val description: String = ""
)