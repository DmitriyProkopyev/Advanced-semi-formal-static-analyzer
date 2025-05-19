package iu.sna.application;

import com.google.gson.JsonArray;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.request.ChatRequest;
import iu.sna.application.llm_stages.ValidationBlock;
import iu.sna.domain.repository_scanner.FileTechnologyStack;
import iu.sna.infrastructure.LLM;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class ValidationBlockBuilder {
  private LLM llm;

  public ValidationBlockBuilder(Collection<FileTechnologyStack> techStack, LLM llm) {
    this.llm = llm;
    // decide the grouping logic here based on the frequency of language-to-tech occurrences

  }

  // то есть для каждго языке и технологии мы сделали standard
  public Collection<ValidationBlock> buildFrom(
          Map<String, List<String>> languages,
          Map<String, List<String>> technologies) {


    String systemPrompt = "You are the expert in analyzing projects for standards and best practices violation";
    String userPrompt = """
                            
                            
                            You are given with the following Map1 : language -> List of standards
                            
                            %s
                            
                            
                            Map2 : technologies -> List of standards:
                            
                            %s
                            
                            You task is to group  (belonging to multiple groups is allowed) the standards into validation
                            blocks, each block describing a set of standards that should be validated together.
                            Requirements: 
                            1.  Ensure that the grouping occurs based on expected coupling between code fragments that
                               will be analyzed
                            2. Minimize the number of validation blocks
                            The output should be in format
                            [
                                
                              "validation_block_name": {
                              languages: ["language1", "language2", "languageN"],
                              technologies: ["tech1", "tech2", "techN"],
                              standards: ["standard1", "standard2", "standardN"]
                              }
                            ...
                            ]
                            and include only json file (with brackets [])
                        """.formatted(languages,technologies);
    String response = this.llm.nextModel()
            .chat(ChatRequest.builder()
                    .messages(UserMessage.from(userPrompt), SystemMessage.from(systemPrompt))
                    .build())
            .aiMessage()
            .toString();
    try {
      JSONArray blocksArray = new JSONArray(response);
      Collection<ValidationBlock> res = new ArrayList<>();
      for (int i = 0; i < blocksArray.length(); i++) {
        JSONObject obj = blocksArray.getJSONObject(i);
        String name = obj.getString("validation_block_name");
        
        List<String> langs = obj.getJSONArray("languages")
            .toList()
            .stream()
            .map(Object::toString)
            .toList();
            
        List<String> techs = obj.getJSONArray("technologies")
            .toList()
            .stream()
            .map(Object::toString)
            .toList();
            
        List<String> standards = obj.getJSONArray("standards")
            .toList()
            .stream()
            .map(Object::toString)
            .toList();

        ValidationBlock block = new ValidationBlock(
            name,
            langs,
            techs,
            String.join("\n", standards),
            this.llm
        );
        
        res.add(block);
      }
      return res;
    } catch (JSONException e) {
      throw new RuntimeException("Error in ValidationBlockBuilder: " + e);
    }

    // use the grouping logic to group together languages and technologies
    // use an LLM to rewrite the standards into groups
  }
}
