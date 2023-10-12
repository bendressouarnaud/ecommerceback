import com.ankk.ecommerce.EcommerceApplication;
import com.ankk.ecommerce.controller.ApiCallController;
import com.ankk.ecommerce.models.Partenaire;
import com.ankk.ecommerce.repositories.PartenaireRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import javax.inject.Inject;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

//@WebMvcTest(controllers =  {ApiCallController.class})
//@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = EcommerceApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
public class ApiCallTest {

    @Inject
    MockMvc mockMvc;
    @MockBean
    PartenaireRepository partenaireRepository;


    // Methods :
    @Test
    public void createPartenaire() throws Exception{
        //given(partenaireRepository.findByIdent(1)).willReturn(new Partenaire());
        when(partenaireRepository.findByIdent(1)).thenReturn(new Partenaire());
        mockMvc.perform(get("/enregistrerPartenaire")
                .param("ident", "1")
                .param("libelle", "ANKK")
                .param("contact", "0506276798")
                .param("email", "ngbandamakonan@gmail.com")
        ).andExpect(status().is2xxSuccessful());
        verify(partenaireRepository).save(any(Partenaire.class));
    }

}
