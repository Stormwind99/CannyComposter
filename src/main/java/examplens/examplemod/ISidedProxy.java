package examplens.examplemod;

import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

public interface ISidedProxy
{
    void preInit(FMLPreInitializationEvent event);

    void init(FMLInitializationEvent event);

    void postInit(FMLPostInitializationEvent event);
    
    /**
     * Thrown when a proxy method is called from the wrong side.
     */
    class WrongSideException extends RuntimeException
    {
        static final long serialVersionUID = 1234L;

        public WrongSideException(final String message)
        {
            super(message);
        }

        public WrongSideException(final String message, final Throwable cause)
        {
            super(message, cause);
        }
    }
}
