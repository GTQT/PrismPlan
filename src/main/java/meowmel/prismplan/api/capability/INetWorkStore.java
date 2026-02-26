package meowmel.prismplan.api.capability;

public interface INetWorkStore extends INetWorkProxy{
    int getPriority();

    void refresh();
}
