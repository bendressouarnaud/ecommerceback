import com.ankk.ecommerce.models.Client;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

//@ExtendWith(SpringExtension.class)
public class ClientTest {

    // A t t r i b u t e s :


    // M E T H O D S :
    @Test
    public void nouveauClient(){
        // 9
        Client ct = new Client();
        ct.setFcmtoken("");
        ct.setAdresse("17 RUE SAINT MARTIN");
        ct.setGenre(1);
        ct.setEmail("azerty@gmail.com");
        ct.setNumero("06276798");
        ct.setNom("YAO");
        ct.setPrenom("Koffi");
        ct.setCommune(1);
        ct.setPwd("1234");

        assertEquals(ct.getPwd(), "1234");

        // Make the call :
        /*given().
                contentType(APPLICATION_JSON).
                body(null).
                when()
                .post("/sendMeasurements")
                .then()
                .assertThat()
                .statusCode(204);*/
    }

}
