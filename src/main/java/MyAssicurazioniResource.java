import java.util.List;
import java.util.concurrent.CompletionStage;

import org.acme.MyAssicurazioniService;
import org.acme.beans.insurance.Insurance;
import org.acme.dto.InsuranceDTO;
import org.acme.dto.VehicleDTO;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;

import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/MyAssicurazioniService")
public class MyAssicurazioniResource {
    @Inject
    MyAssicurazioniService service;

    @Inject
    JsonWebToken jwt;

    @Inject
    @Channel("my-topic-insurance")
    Emitter<String> insuranceEmitter;
 

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @RolesAllowed({ "User", "Admin" }) 
    @Path("/Create")
    @Transactional
    public Response createInsurance(VehicleDTO vehicleDTO){
        try{
            Insurance id = service.createInsurance(vehicleDTO);
            insuranceEmitter.send(id.toString()).toCompletableFuture().join();
            String message = "{\"id\":"+ id + ",\"message\": "+"\"Inserimento avvenuto correttamente\"}";
            return Response.status(Response.Status.OK).type(MediaType.APPLICATION_JSON).entity(message).build();
        }catch(Exception e){
            String message = "{\"errorCode\":5,\"message\": "+"\"Errore nell'operazione di update\"}";
            return Response.status(Response.Status.NOT_MODIFIED)
            .entity(message)
            .build();
        }
    }
    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/Update")
    @RolesAllowed({ "User", "Admin" }) 
    @Transactional
    public Response updateInsurance(InsuranceDTO insurance){
        try {
            service.updateInsurance(insurance);
            String message = "{\"message\": "+"\"Update effettuato correttamente\"}";
            return Response.ok().type(MediaType.APPLICATION_JSON).entity(message).build();
        } catch (Exception e) {
            String message = "{\"errorCode\":10,\"message\": "+"\"Errore nell'operazione di update\"}";
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
            .entity(message).type(MediaType.APPLICATION_JSON)
            .build();        
        }
           
    }
    @GET
    @Path("/GetInsuranceByTarga")
    @RolesAllowed({ "User", "Admin" }) 
    @Produces(MediaType.APPLICATION_JSON)
    @Transactional
    public Response getInsuranceByTarga(@QueryParam("targa") String targa){
        try {
            List<Insurance> insuranceList;
            Insurance insurance;
            if(targa != null){
                insurance = service.getInsurancebyTarga(targa);
                return Response.ok(insurance).build();

            }else{
                insuranceList = service.getAllInsurances();
                return Response.ok(insuranceList).build();

            }
        } catch (Exception e) {
            String message = "{\"errorCode\":15,\"message\": "+"\""+e.getMessage()+ "\"}";
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
            .type(MediaType.APPLICATION_JSON)
            .entity(message)
            .build();
        }
           
    }
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({ "User", "Admin" }) 
    @Path("/GetAllInsurance")
    @Transactional
    public Response getAllInsurance(){
        try {
            List<Insurance> insuranceList= service.getAllInsurances();
            return Response.ok(insuranceList).build();
        } catch (Exception e) {
            String message = "{\"errorCode\":20,\"message\": "+"\""+e.getMessage()+ "\"}";
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
            .type(MediaType.APPLICATION_JSON)
            .entity(message)
            .build();
        }
           
    }
    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({ "User", "Admin" }) 
    @Path("/DeleteInsurance")
    @Transactional
    public Response DeleteInsuranceByTarga(@QueryParam("targa") String targa){
        try {
            if(targa != null){
                service.deleteInsuranceByTarga(targa);
                return Response.status(200,"Eliminazione effettuata").build();
            }else{
                return Response.notModified().build();
            }
        } catch (Exception e) {
            String message = "{\"errorCode\":25,\"message\": "+"\""+e.getMessage()+ "\"}";
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).
            type(MediaType.APPLICATION_JSON).
            entity(message).build();
        }
           
    }


}
