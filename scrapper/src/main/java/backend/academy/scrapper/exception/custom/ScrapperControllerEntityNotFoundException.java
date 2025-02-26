package backend.academy.scrapper.exception.custom;

public class ScrapperControllerEntityNotFoundException extends ScrapperException{

    @Override
    public int getStatus(){
        return 404;
    }

    public ScrapperControllerEntityNotFoundException(Throwable cause, String message) {
        super(message);
    }
}
