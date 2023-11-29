-- public.users definition
CREATE TABLE IF NOT EXISTS public.users (
	id uuid NOT NULL,
	email varchar(255) NULL,
	"name" varchar(255) NULL,
	"password" varchar(255) NULL,
	"role" varchar(255) NULL,
	CONSTRAINT uk_3g1j96g94xpk3lpxl2qbl985x UNIQUE (name),
	CONSTRAINT uk_6dotkott2kjsp8vw4d0m25fb7 UNIQUE (email),
	CONSTRAINT users_pkey PRIMARY KEY (id),
	CONSTRAINT users_role_check CHECK (((role)::text = ANY ((ARRAY['USER'::character varying, 'ADMIN'::character varying])::text[])))
);

-- public.resource definition
CREATE TABLE IF NOT EXISTS public.resource (
	dtype varchar(31) NOT NULL,
	id uuid NOT NULL,
	clazz varchar(255) NULL,
	note varchar(255) NULL,
	price_per_quantity float8 NULL,
	quantity_type varchar(255) NULL,
	description varchar(255) NULL,
	color varchar(255) NULL,
	plating varchar(255) NULL,
	purity int4 NULL,
	"type" varchar(255) NULL,
	quality varchar(255) NULL,
	shape varchar(255) NULL,
	"size" float8 NULL,
	carat float8 NULL,
	clarity varchar(255) NULL,
	cut varchar(255) NULL,
	dimensionx float8 NULL,
	dimensiony float8 NULL,
	dimensionz float8 NULL,
	CONSTRAINT resource_pkey PRIMARY KEY (id)
);

-- public.resource_in_user definition
CREATE TABLE IF NOT EXISTS public.resource_in_user (
	id uuid NOT NULL,
	quantity float8 NOT NULL,
	owner_id uuid NULL,
	resource_id uuid NULL,
	CONSTRAINT resource_in_user_pkey PRIMARY KEY (id),
	CONSTRAINT fk2o2wr3g91b4vetixnqsjrcgou FOREIGN KEY (resource_id) REFERENCES public.resource(id),
	CONSTRAINT fkdvwaulnt1px4lb1hvpp829hgj FOREIGN KEY (owner_id) REFERENCES public.users(id)
);

-- public.image definition
CREATE TABLE IF NOT EXISTS public.image (
	id uuid NOT NULL,
	file_path varchar(255) NULL,
	"name" varchar(255) NULL,
	"type" varchar(255) NULL,
	CONSTRAINT image_pkey PRIMARY KEY (id)
);

-- public.sale definition
CREATE TABLE IF NOT EXISTS public.sale (
	id uuid NOT NULL,
	"date" timestamp(6) NULL,
	buyer_id uuid NULL,
	seller_id uuid NULL,
	CONSTRAINT sale_pkey PRIMARY KEY (id),
	CONSTRAINT fk155rrg9pdabtfo9kg9op6lxmg FOREIGN KEY (buyer_id) REFERENCES public.users(id),
	CONSTRAINT fkifik8a9kgtivlcsgrdy712j8 FOREIGN KEY (seller_id) REFERENCES public.users(id)
);

-- public.product definition
CREATE TABLE IF NOT EXISTS public.product (
	id uuid NOT NULL,
	catalog_number varchar(255) NULL,
	description varchar(255) NULL,
	discount float8 NULL,
	production_number varchar(255) NULL,
	sale_price float8 NULL,
	content_of uuid NULL,
	image_id uuid NULL,
	owner_id uuid NULL,
	part_of_sale_id uuid NULL,
	CONSTRAINT product_pkey PRIMARY KEY (id),
	CONSTRAINT uk_1obwxotxqdp078249sw6gq4oj UNIQUE (image_id),
	CONSTRAINT fk4uir3sxob7667q1xjqfrrurmh FOREIGN KEY (owner_id) REFERENCES public.users(id),
	CONSTRAINT fkl3a5o6wwot4m1qemnjiv92cdn FOREIGN KEY (part_of_sale_id) REFERENCES public.sale(id),
	CONSTRAINT fkmcg91uti27fhbblavtblihbe6 FOREIGN KEY (content_of) REFERENCES public.product(id),
	CONSTRAINT fksouy49035ik9r5ojgslbv3i3u FOREIGN KEY (image_id) REFERENCES public.image(id)
);

-- public.resource_in_product definition
CREATE TABLE IF NOT EXISTS public.resource_in_product (
	id uuid NOT NULL,
	quantity float8 NULL,
	product_id uuid NULL,
	resource_id uuid NULL,
	CONSTRAINT resource_in_product_pkey PRIMARY KEY (id),
	CONSTRAINT fk9ilwwst211t6apwy1ukqyg7tj FOREIGN KEY (product_id) REFERENCES public.product(id),
	CONSTRAINT fkh3uo08k72anhr7lop9axrl8qt FOREIGN KEY (resource_id) REFERENCES public.resource(id)
);

-- public.product_author definition
CREATE TABLE IF NOT EXISTS public.product_author (
	product_id uuid NOT NULL,
	user_id uuid NOT NULL,
	CONSTRAINT fkalmj7y7ofoxxlqyvspperm99l FOREIGN KEY (product_id) REFERENCES public.product(id),
	CONSTRAINT fknidyefynbw5ey4wk278derpr7 FOREIGN KEY (user_id) REFERENCES public.users(id)
);

-- public.system_event definition
CREATE TABLE IF NOT EXISTS public.system_event (
	id uuid NOT NULL,
	"data" jsonb NULL,
	"timestamp" timestamp(6) NULL,
	"type" varchar(255) NULL,
	CONSTRAINT system_event_pkey PRIMARY KEY (id)
);