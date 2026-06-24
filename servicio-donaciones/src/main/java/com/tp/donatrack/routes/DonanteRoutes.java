package com.tp.donatrack.routes;

public class DonanteRoutes {
        private DonanteRoutes() {}

        public static final String BASE             = "/donantes";
        public static final String POR_EMAIL           = "/{email}";
        public static final String HUMANO           = "/humano";
        public static final String JURIDICO         = "/juridico";
        public static final String ACTUALIZAR_HUMANO  = "/{email}/humano";
        public static final String ACTUALIZAR_JURIDICO = "/{email}/juridico";

}
