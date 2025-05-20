--
-- PostgreSQL database dump
--

-- Dumped from database version 17.4
-- Dumped by pg_dump version 17.4

-- Started on 2025-05-20 18:05:39

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET transaction_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

SET default_tablespace = '';

SET default_table_access_method = heap;

--
-- TOC entry 220 (class 1259 OID 16420)
-- Name: adminprofiles; Type: TABLE; Schema: public; Owner: approject-admin
--

CREATE TABLE public.adminprofiles (
    id bigint NOT NULL,
    address character varying(255),
    fullname character varying(255) NOT NULL,
    imageurl character varying(255),
    phonenumber character varying(255) NOT NULL,
    userid bigint NOT NULL
);


ALTER TABLE public.adminprofiles OWNER TO "approject-admin";

--
-- TOC entry 225 (class 1259 OID 16438)
-- Name: adminprofiles_id_seq; Type: SEQUENCE; Schema: public; Owner: approject-admin
--

CREATE SEQUENCE public.adminprofiles_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.adminprofiles_id_seq OWNER TO "approject-admin";

--
-- TOC entry 4961 (class 0 OID 0)
-- Dependencies: 225
-- Name: adminprofiles_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: approject-admin
--

ALTER SEQUENCE public.adminprofiles_id_seq OWNED BY public.adminprofiles.id;


--
-- TOC entry 223 (class 1259 OID 16432)
-- Name: basketitems; Type: TABLE; Schema: public; Owner: approject-admin
--

CREATE TABLE public.basketitems (
    id bigint NOT NULL,
    basketid bigint NOT NULL,
    foodid bigint NOT NULL,
    foodname character varying(255),
    price double precision,
    quantity integer
);


ALTER TABLE public.basketitems OWNER TO "approject-admin";

--
-- TOC entry 226 (class 1259 OID 16445)
-- Name: basketitems_id_seq; Type: SEQUENCE; Schema: public; Owner: approject-admin
--

CREATE SEQUENCE public.basketitems_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.basketitems_id_seq OWNER TO "approject-admin";

--
-- TOC entry 4962 (class 0 OID 0)
-- Dependencies: 226
-- Name: basketitems_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: approject-admin
--

ALTER SEQUENCE public.basketitems_id_seq OWNED BY public.basketitems.id;


--
-- TOC entry 222 (class 1259 OID 16429)
-- Name: baskets; Type: TABLE; Schema: public; Owner: approject-admin
--

CREATE TABLE public.baskets (
    basketid bigint NOT NULL,
    totalprice double precision NOT NULL,
    userid bigint NOT NULL
);


ALTER TABLE public.baskets OWNER TO "approject-admin";

--
-- TOC entry 227 (class 1259 OID 16450)
-- Name: baskets_basketid_seq; Type: SEQUENCE; Schema: public; Owner: approject-admin
--

CREATE SEQUENCE public.baskets_basketid_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.baskets_basketid_seq OWNER TO "approject-admin";

--
-- TOC entry 4963 (class 0 OID 0)
-- Dependencies: 227
-- Name: baskets_basketid_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: approject-admin
--

ALTER SEQUENCE public.baskets_basketid_seq OWNED BY public.baskets.basketid;


--
-- TOC entry 229 (class 1259 OID 16456)
-- Name: courtierprofiles; Type: TABLE; Schema: public; Owner: approject-admin
--

CREATE TABLE public.courtierprofiles (
    id bigint NOT NULL,
    address character varying(255),
    fullname character varying(255) NOT NULL,
    imageurl character varying(255),
    phone character varying(255) NOT NULL,
    userid bigint NOT NULL,
    bankinfo character varying(255),
    status smallint,
    CONSTRAINT courtierprofiles_status_check CHECK (((status >= 0) AND (status <= 1)))
);


ALTER TABLE public.courtierprofiles OWNER TO "approject-admin";

--
-- TOC entry 228 (class 1259 OID 16455)
-- Name: courtierprofiles_id_seq; Type: SEQUENCE; Schema: public; Owner: approject-admin
--

CREATE SEQUENCE public.courtierprofiles_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.courtierprofiles_id_seq OWNER TO "approject-admin";

--
-- TOC entry 4964 (class 0 OID 0)
-- Dependencies: 228
-- Name: courtierprofiles_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: approject-admin
--

ALTER SEQUENCE public.courtierprofiles_id_seq OWNED BY public.courtierprofiles.id;


--
-- TOC entry 219 (class 1259 OID 16417)
-- Name: customerprofiles; Type: TABLE; Schema: public; Owner: approject-admin
--

CREATE TABLE public.customerprofiles (
    id bigint NOT NULL,
    address character varying(255),
    fullname character varying(255) NOT NULL,
    imageurl character varying(255),
    phone character varying(255) NOT NULL,
    userid bigint NOT NULL
);


ALTER TABLE public.customerprofiles OWNER TO "approject-admin";

--
-- TOC entry 230 (class 1259 OID 16465)
-- Name: customerprofiles_id_seq; Type: SEQUENCE; Schema: public; Owner: approject-admin
--

CREATE SEQUENCE public.customerprofiles_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.customerprofiles_id_seq OWNER TO "approject-admin";

--
-- TOC entry 4965 (class 0 OID 0)
-- Dependencies: 230
-- Name: customerprofiles_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: approject-admin
--

ALTER SEQUENCE public.customerprofiles_id_seq OWNED BY public.customerprofiles.id;


--
-- TOC entry 221 (class 1259 OID 16426)
-- Name: ownerprofiles; Type: TABLE; Schema: public; Owner: approject-admin
--

CREATE TABLE public.ownerprofiles (
    id bigint NOT NULL,
    address character varying(255),
    fullname character varying(255) NOT NULL,
    imageurl character varying(255),
    phone character varying(255) NOT NULL,
    userid bigint NOT NULL,
    bankinfo character varying(255),
    brandname character varying(255),
    explanation character varying(255)
);


ALTER TABLE public.ownerprofiles OWNER TO "approject-admin";

--
-- TOC entry 231 (class 1259 OID 16472)
-- Name: ownerprofiles_id_seq; Type: SEQUENCE; Schema: public; Owner: approject-admin
--

CREATE SEQUENCE public.ownerprofiles_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.ownerprofiles_id_seq OWNER TO "approject-admin";

--
-- TOC entry 4966 (class 0 OID 0)
-- Dependencies: 231
-- Name: ownerprofiles_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: approject-admin
--

ALTER SEQUENCE public.ownerprofiles_id_seq OWNED BY public.ownerprofiles.id;


--
-- TOC entry 217 (class 1259 OID 16402)
-- Name: users; Type: TABLE; Schema: public; Owner: approject-admin
--

CREATE TABLE public.users (
    id bigint NOT NULL,
    address character varying(255),
    email character varying(255),
    fullname character varying(255) NOT NULL,
    password character varying(255) NOT NULL,
    phone character varying(255) NOT NULL,
    restaurantid bigint,
    role character varying(255),
    CONSTRAINT users_role_check CHECK (((role)::text = ANY ((ARRAY['CUSTOMER'::character varying, 'COURTIER'::character varying, 'ADMIN'::character varying, 'OWNER'::character varying])::text[])))
);


ALTER TABLE public.users OWNER TO "approject-admin";

--
-- TOC entry 218 (class 1259 OID 16405)
-- Name: users_id_seq; Type: SEQUENCE; Schema: public; Owner: approject-admin
--

CREATE SEQUENCE public.users_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.users_id_seq OWNER TO "approject-admin";

--
-- TOC entry 4967 (class 0 OID 0)
-- Dependencies: 218
-- Name: users_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: approject-admin
--

ALTER SEQUENCE public.users_id_seq OWNED BY public.users.id;


--
-- TOC entry 224 (class 1259 OID 16435)
-- Name: wallets; Type: TABLE; Schema: public; Owner: approject-admin
--

CREATE TABLE public.wallets (
    id bigint NOT NULL,
    balance double precision NOT NULL,
    userid bigint NOT NULL
);


ALTER TABLE public.wallets OWNER TO "approject-admin";

--
-- TOC entry 232 (class 1259 OID 16479)
-- Name: wallets_id_seq; Type: SEQUENCE; Schema: public; Owner: approject-admin
--

CREATE SEQUENCE public.wallets_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.wallets_id_seq OWNER TO "approject-admin";

--
-- TOC entry 4968 (class 0 OID 0)
-- Dependencies: 232
-- Name: wallets_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: approject-admin
--

ALTER SEQUENCE public.wallets_id_seq OWNED BY public.wallets.id;


--
-- TOC entry 4779 (class 2604 OID 16439)
-- Name: adminprofiles id; Type: DEFAULT; Schema: public; Owner: approject-admin
--

ALTER TABLE ONLY public.adminprofiles ALTER COLUMN id SET DEFAULT nextval('public.adminprofiles_id_seq'::regclass);


--
-- TOC entry 4782 (class 2604 OID 16446)
-- Name: basketitems id; Type: DEFAULT; Schema: public; Owner: approject-admin
--

ALTER TABLE ONLY public.basketitems ALTER COLUMN id SET DEFAULT nextval('public.basketitems_id_seq'::regclass);


--
-- TOC entry 4781 (class 2604 OID 16451)
-- Name: baskets basketid; Type: DEFAULT; Schema: public; Owner: approject-admin
--

ALTER TABLE ONLY public.baskets ALTER COLUMN basketid SET DEFAULT nextval('public.baskets_basketid_seq'::regclass);


--
-- TOC entry 4784 (class 2604 OID 16459)
-- Name: courtierprofiles id; Type: DEFAULT; Schema: public; Owner: approject-admin
--

ALTER TABLE ONLY public.courtierprofiles ALTER COLUMN id SET DEFAULT nextval('public.courtierprofiles_id_seq'::regclass);


--
-- TOC entry 4778 (class 2604 OID 16466)
-- Name: customerprofiles id; Type: DEFAULT; Schema: public; Owner: approject-admin
--

ALTER TABLE ONLY public.customerprofiles ALTER COLUMN id SET DEFAULT nextval('public.customerprofiles_id_seq'::regclass);


--
-- TOC entry 4780 (class 2604 OID 16473)
-- Name: ownerprofiles id; Type: DEFAULT; Schema: public; Owner: approject-admin
--

ALTER TABLE ONLY public.ownerprofiles ALTER COLUMN id SET DEFAULT nextval('public.ownerprofiles_id_seq'::regclass);


--
-- TOC entry 4777 (class 2604 OID 16406)
-- Name: users id; Type: DEFAULT; Schema: public; Owner: approject-admin
--

ALTER TABLE ONLY public.users ALTER COLUMN id SET DEFAULT nextval('public.users_id_seq'::regclass);


--
-- TOC entry 4783 (class 2604 OID 16480)
-- Name: wallets id; Type: DEFAULT; Schema: public; Owner: approject-admin
--

ALTER TABLE ONLY public.wallets ALTER COLUMN id SET DEFAULT nextval('public.wallets_id_seq'::regclass);


--
-- TOC entry 4802 (class 2606 OID 16464)
-- Name: courtierprofiles courtierprofiles_pkey; Type: CONSTRAINT; Schema: public; Owner: approject-admin
--

ALTER TABLE ONLY public.courtierprofiles
    ADD CONSTRAINT courtierprofiles_pkey PRIMARY KEY (id);


--
-- TOC entry 4792 (class 2606 OID 16491)
-- Name: adminprofiles uk_16x0du3s38e290wsk1xbouhh5; Type: CONSTRAINT; Schema: public; Owner: approject-admin
--

ALTER TABLE ONLY public.adminprofiles
    ADD CONSTRAINT uk_16x0du3s38e290wsk1xbouhh5 UNIQUE (userid);


--
-- TOC entry 4800 (class 2606 OID 16493)
-- Name: baskets uk_3nfnqtjumytplgkdf9gy7dqfm; Type: CONSTRAINT; Schema: public; Owner: approject-admin
--

ALTER TABLE ONLY public.baskets
    ADD CONSTRAINT uk_3nfnqtjumytplgkdf9gy7dqfm UNIQUE (userid);


--
-- TOC entry 4794 (class 2606 OID 16487)
-- Name: adminprofiles uk_7fdkojlnth0j89j6i5pitwxe6; Type: CONSTRAINT; Schema: public; Owner: approject-admin
--

ALTER TABLE ONLY public.adminprofiles
    ADD CONSTRAINT uk_7fdkojlnth0j89j6i5pitwxe6 UNIQUE (imageurl);


--
-- TOC entry 4804 (class 2606 OID 16495)
-- Name: courtierprofiles uk_a18nfcefglu9obqlmxf520isc; Type: CONSTRAINT; Schema: public; Owner: approject-admin
--

ALTER TABLE ONLY public.courtierprofiles
    ADD CONSTRAINT uk_a18nfcefglu9obqlmxf520isc UNIQUE (address);


--
-- TOC entry 4788 (class 2606 OID 16416)
-- Name: users uk_eal2jh4gm2liwl0dd5lxl5i4d; Type: CONSTRAINT; Schema: public; Owner: approject-admin
--

ALTER TABLE ONLY public.users
    ADD CONSTRAINT uk_eal2jh4gm2liwl0dd5lxl5i4d UNIQUE (restaurantid);


--
-- TOC entry 4806 (class 2606 OID 16499)
-- Name: courtierprofiles uk_eq9wp8ruls9vnehio7478nyfc; Type: CONSTRAINT; Schema: public; Owner: approject-admin
--

ALTER TABLE ONLY public.courtierprofiles
    ADD CONSTRAINT uk_eq9wp8ruls9vnehio7478nyfc UNIQUE (phone);


--
-- TOC entry 4796 (class 2606 OID 16485)
-- Name: adminprofiles uk_k8jobty1i9cbfwh0mk5na46hh; Type: CONSTRAINT; Schema: public; Owner: approject-admin
--

ALTER TABLE ONLY public.adminprofiles
    ADD CONSTRAINT uk_k8jobty1i9cbfwh0mk5na46hh UNIQUE (address);


--
-- TOC entry 4808 (class 2606 OID 16501)
-- Name: courtierprofiles uk_kmxkxe6hlknh9i4qlarp1nd2s; Type: CONSTRAINT; Schema: public; Owner: approject-admin
--

ALTER TABLE ONLY public.courtierprofiles
    ADD CONSTRAINT uk_kmxkxe6hlknh9i4qlarp1nd2s UNIQUE (userid);


--
-- TOC entry 4790 (class 2606 OID 16414)
-- Name: users uk_kwds03ohobcd8p6eowkw0f5bm; Type: CONSTRAINT; Schema: public; Owner: approject-admin
--

ALTER TABLE ONLY public.users
    ADD CONSTRAINT uk_kwds03ohobcd8p6eowkw0f5bm UNIQUE (phone);


--
-- TOC entry 4798 (class 2606 OID 16489)
-- Name: adminprofiles uk_mji9tmeu8q7hofg4g2ssf1v38; Type: CONSTRAINT; Schema: public; Owner: approject-admin
--

ALTER TABLE ONLY public.adminprofiles
    ADD CONSTRAINT uk_mji9tmeu8q7hofg4g2ssf1v38 UNIQUE (phonenumber);


--
-- TOC entry 4810 (class 2606 OID 16497)
-- Name: courtierprofiles uk_ml4y1q7d1wnqmjx7ki3hkp09f; Type: CONSTRAINT; Schema: public; Owner: approject-admin
--

ALTER TABLE ONLY public.courtierprofiles
    ADD CONSTRAINT uk_ml4y1q7d1wnqmjx7ki3hkp09f UNIQUE (imageurl);


-- Completed on 2025-05-20 18:05:40

--
-- PostgreSQL database dump complete
--

