package red.jackf.whereisit.command;

import red.jackf.jackfredlib.api.extracommandsourcedata.ExtraSourceData;
import red.jackf.whereisit.WhereIsIt;
import red.jackf.whereisit.api.SearchRequest;
import red.jackf.whereisit.api.criteria.Criterion;

import java.util.ArrayList;
import java.util.List;

public class CommandCriteria implements ExtraSourceData<CommandCriteria> {
    public static final Definition<CommandCriteria>  DEFINITION = new Definition<>(WhereIsIt.id("criteria"),
            CommandCriteria.class,
            CommandCriteria::new);
    private final List<Criterion> criteria = new ArrayList<>();

    public void addCriterion(Criterion criterion) {
        this.criteria.add(criterion);
    }

    public SearchRequest toRequest() {
        var request = new SearchRequest();
        this.criteria.forEach(request);
        return request;
    }

    @Override
    public CommandCriteria copy() {
        var copy = new CommandCriteria();
        copy.criteria.addAll(this.criteria);
        return copy;
    }
}
