package com.github.DiachenkoMD.web.utils.middlewares.origins;

/**
 * Acts like marker class for wards. Wards, unlike guards, don`t protect routes. They are designed to process data and execute some actions.<br/>
 * All children of that class can be passed to {@link com.github.DiachenkoMD.web.utils.middlewares.warden.UseWards @UseWards}.
 */
public abstract non-sealed class Ward extends Chainable{}
