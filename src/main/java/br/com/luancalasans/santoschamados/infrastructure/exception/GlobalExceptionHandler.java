package br.com.luancalasans.santoschamados.infrastructure.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.ui.Model;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(BusinessException.class)
    public String handleBusiness(BusinessException ex, HttpServletRequest request, RedirectAttributes ra, Model model) {
        if (isAjax(request)) {
            model.addAttribute("mensagem", ex.getMessage());
            return "fragments/alert :: alert";
        }
        ra.addFlashAttribute("erro", ex.getMessage());
        String referer = request.getHeader("Referer");
        return "redirect:" + (referer != null ? referer.replaceFirst("https?://[^/]+", "") : "/");
    }

    @ExceptionHandler({MethodArgumentNotValidException.class, BindException.class})
    public String handleValidation(Exception ex, HttpServletRequest request, RedirectAttributes ra) {
        ra.addFlashAttribute("erro", "Verifique os campos obrigatórios e tente novamente.");
        String referer = request.getHeader("Referer");
        return "redirect:" + (referer != null ? referer.replaceFirst("https?://[^/]+", "") : "/");
    }

    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public String handleAccessDenied(AccessDeniedException ex, Model model) {
        model.addAttribute("titulo", "Acesso negado");
        model.addAttribute("mensagem", "Você não possui permissão para acessar este recurso.");
        return "error/acesso-negado";
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleNotFound(ResourceNotFoundException ex, Model model) {
        model.addAttribute("titulo", "Não encontrado");
        model.addAttribute("mensagem", "O recurso solicitado não foi encontrado.");
        return "error/nao-encontrado";
    }

    @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
    public String handleOptimistic(ObjectOptimisticLockingFailureException ex, RedirectAttributes ra, HttpServletRequest request) {
        ra.addFlashAttribute("erro", "O chamado foi alterado por outro usuário. Atualize a página e tente novamente.");
        String referer = request.getHeader("Referer");
        return "redirect:" + (referer != null ? referer.replaceFirst("https?://[^/]+", "") : "/chamados");
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public String handleUploadSize(MaxUploadSizeExceededException ex, RedirectAttributes ra, HttpServletRequest request) {
        ra.addFlashAttribute("erro", "O arquivo enviado excede o tamanho máximo permitido.");
        String referer = request.getHeader("Referer");
        return "redirect:" + (referer != null ? referer.replaceFirst("https?://[^/]+", "") : "/chamados/novo");
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String handleGeneric(Exception ex, Model model) {
        log.error("Erro não tratado", ex);
        model.addAttribute("titulo", "Erro interno");
        model.addAttribute("mensagem", "Ocorreu um erro inesperado. Tente novamente mais tarde.");
        return "error/generico";
    }

    private boolean isAjax(HttpServletRequest request) {
        String requestedWith = request.getHeader("X-Requested-With");
        return "XMLHttpRequest".equalsIgnoreCase(requestedWith);
    }
}
